package uk.gov.hmcts.reform.et.syaapi.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.et.syaapi.client.CcdApiClient;
import uk.gov.hmcts.reform.et.syaapi.models.EmploymentCaseData;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

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

    /**
     * Given a caseID, this will retrieve the correct {@link CaseDetails}.
     *
     * @param authorization is used to seek the {@link UserDetails} for request
     * @param caseType is used to determine if the case is for ET_EnglandWales or ET_Scotland
     * @param eventType is used to determine initiateCaseDraft or initiateCase
     * @param caseData is used to provide the {@link EmploymentCaseData} in json format
     * @return the associated {@link CaseDetails} if the case is created
     * @throws Exception if {@link CaseDetails} cannot be created
     */
    @Retryable({FeignException.class, RuntimeException.class})
    public CaseDetails createCase(String authorization, String caseType, String eventType, String caseData) {
        EmploymentCaseData data = getEmploymentCaseData(caseData);
        String s2sToken = authTokenGenerator.generate();
        UserDetails userDetails = idamClient.getUserDetails(authorization);
        // Temporarily returning hardcoded userId while Idam implementation is worked on
        var userID = userDetails.getId() == null ? "123456" : userDetails.getId();
        var ccdCase = ccdApiClient.startForCaseworker(
            authorization,
            s2sToken,
            userID,
            JURISDICTION_ID,
            caseType,
            eventType
        );
        CaseDataContent caseDataContent = CaseDataContent.builder()
            .event(Event.builder().id(eventType).build())
            .eventToken(ccdCase.getToken())
            .data(data)
            .build();
        return ccdApiClient.submitForCaseworker(
            authorization,
            s2sToken,
            userID,
            JURISDICTION_ID,
            caseType,
            true,
            caseDataContent
        );
    }

    private EmploymentCaseData getEmploymentCaseData(String caseData) {
        ObjectMapper mapper = new ObjectMapper();
        EmploymentCaseData data = null;
        try {
            data = mapper.readValue(caseData, EmploymentCaseData.class);
        } catch (JsonProcessingException e) {
            log.error(e.getMessage());
        }
        return data;
    }
}
