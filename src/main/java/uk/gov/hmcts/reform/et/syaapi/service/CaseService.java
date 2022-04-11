package uk.gov.hmcts.reform.et.syaapi.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ecm.common.model.ccd.CaseData;
import uk.gov.hmcts.ecm.common.model.ccd.Et1CaseData;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.et.syaapi.client.CcdApiClient;
import uk.gov.hmcts.reform.et.syaapi.enums.CaseEvent;
import uk.gov.hmcts.reform.et.syaapi.helper.CaseDetailsConverter;
import uk.gov.hmcts.reform.et.syaapi.helper.EmployeeObjectMapper;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.util.List;

import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.JURISDICTION_ID;

@Slf4j
@Service
public class CaseService {

    @Autowired
    private AuthTokenGenerator authTokenGenerator;

    @Autowired
    private CcdApiClient ccdApiClient;

    @Autowired
    private IdamClient idamClient;

    @Autowired
    private CaseDetailsConverter caseDetailsConverter;

    @Autowired
    private EmployeeObjectMapper employeeObjectMapper;

    /**
     * Given a caseID, this will retrieve the correct {@link CaseDetails}.
     *
     * @param caseId is the identifier to seek the {@link CaseDetails} for
     * @return the associated {@link CaseDetails} for the ID provided
     * @throws Exception if {@link CaseDetails} don't exist for the ID given
     */
    @Retryable({FeignException.class, RuntimeException.class})
    public CaseDetails getCaseData(String authorization, String caseId) {
        return ccdApiClient.getCase(authorization, authTokenGenerator.generate(), caseId);
    }

    @Retryable({FeignException.class, RuntimeException.class})
    public List<CaseDetails> getCaseDataByUser(String authorization, String caseType, String searchString) {
        return ccdApiClient.searchCases(
            authorization, authTokenGenerator.generate(), caseType, searchString).getCases();
    }

    /**
     * Given a caseID, this will retrieve the correct {@link CaseDetails}.
     *
     * @param authorization is used to seek the {@link UserDetails} for request
     * @param caseType is used to determine if the case is for ET_EnglandWales or ET_Scotland
     * @param eventType is used to determine initiateCaseDraft or initiateCase
     * @param caseData is used to provide the {@link Et1CaseData} in json format
     * @return the associated {@link CaseDetails} if the case is created
     * @throws Exception if {@link CaseDetails} cannot be created
     */
    @Retryable({FeignException.class, RuntimeException.class})
    public CaseDetails createCase(String authorization, String caseType, String eventType, String caseData) {
        log.info("Creating Case");
        Et1CaseData data = getEmploymentCaseData(caseData);
        String s2sToken = authTokenGenerator.generate();
        log.info("Generated s2s");
        UserDetails userDetails = idamClient.getUserDetails(authorization);
        log.info("User Id: " + userDetails.getId());
        log.info("Roles : " + userDetails.getRoles());
        var ccdCase = ccdApiClient.startForCaseworker(
            authorization,
            s2sToken,
            userDetails.getId(),
            JURISDICTION_ID,
            caseType,
            eventType
        );
        log.info("Started Case: " + ccdCase.getEventId());
        CaseDataContent caseDataContent = CaseDataContent.builder()
            .event(Event.builder().id(eventType).build())
            .eventToken(ccdCase.getToken())
            .data(data)
            .build();
        return ccdApiClient.submitForCaseworker(
            authorization,
            s2sToken,
            userDetails.getId(),
            JURISDICTION_ID,
            caseType,
            true,
            caseDataContent
        );
    }

    public CaseData triggerEvent(String authorization, String caseId, String caseType,
                                 CaseEvent eventName, String caseData) {
        return triggerEvent(authorization, caseId, eventName, caseType, caseData);
    }

    public CaseData triggerEvent(String authorization, String caseId, CaseEvent eventName,
                                 String caseType, String caseData) {
        StartEventResponse startEventResponse = startUpdate(authorization, caseId, caseType, eventName);
        return submitUpdate(authorization, caseId,
                            caseDetailsConverter.caseDataContent(startEventResponse,
                            employeeObjectMapper.getEmploymentCaseData(caseData)),
                            caseType);
    }

    public StartEventResponse startUpdate(String authorization, String caseId, String caseType, CaseEvent eventName) {
        String s2sToken = authTokenGenerator.generate();
        UserDetails userDetails = idamClient.getUserDetails(authorization);

        return ccdApiClient.startEventForCaseWorker(
            authorization,
            s2sToken,
            userDetails.getId(),
            JURISDICTION_ID,
            caseType,
            caseId,
            eventName.name()
        );
    }

    public CaseData submitUpdate(String authorization, String caseId,
                                 CaseDataContent caseDataContent, String caseType) {
        UserDetails userDetails = idamClient.getUserDetails(authorization);
        String s2sToken = authTokenGenerator.generate();
        CaseDetails caseDetails = ccdApiClient.submitEventForCaseWorker(
            authorization,
            s2sToken,
            userDetails.getId(),
            JURISDICTION_ID,
            caseType,
            caseId,
            true,
            caseDataContent
        );
        return caseDetailsConverter.toCaseData(caseDetails);
    }

    private Et1CaseData getEmploymentCaseData(String caseData) {
        ObjectMapper mapper = new ObjectMapper();
        Et1CaseData data = null;
        try {
            data = mapper.readValue(caseData, Et1CaseData.class);
        } catch (JsonProcessingException e) {
            log.error(e.getMessage());
        }
        return data;
    }
}
