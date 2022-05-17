package uk.gov.hmcts.reform.et.syaapi.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import uk.gov.dwp.regex.InvalidPostcodeException;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.Et1CaseData;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.et.syaapi.enums.CaseEvent;
import uk.gov.hmcts.reform.et.syaapi.helper.CaseDetailsConverter;
import uk.gov.hmcts.reform.et.syaapi.helper.EmployeeObjectMapper;
import uk.gov.hmcts.reform.et.syaapi.models.CaseRequest;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static uk.gov.hmcts.ecm.common.model.helper.TribunalOffice.getCaseTypeId;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.DEFAULT_TRIBUNAL_OFFICE;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.ENGLAND_CASE_TYPE;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.JURISDICTION_ID;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.SCOTLAND_CASE_TYPE;

@Slf4j
@Service
@RequiredArgsConstructor
public class CaseService {

    private final AuthTokenGenerator authTokenGenerator;

    private final CoreCaseDataApi ccdApiClient;

    private final IdamClient idamClient;

    private final PostcodeToOfficeService postcodeToOfficeService;

    /**
     * Given a case id in the case request, this will retrieve the correct {@link CaseDetails}.
     *
     * @param caseRequest contains case id get the {@link CaseDetails} for
     * @return the associated {@link CaseDetails} for the ID provided

     */
    @Retryable({FeignException.class, RuntimeException.class})
    public CaseDetails getUserCase(String authorization, CaseRequest caseRequest) {
        return ccdApiClient.getCase(authorization, authTokenGenerator.generate(), caseRequest.getCaseId());
    }

    /**
     * Given a user derived from the authorisation token in the request,
     * this will get all cases {@link CaseDetails} for that user.
     *
     * @param authorization is used to get the {@link UserDetails} for the request
     * @return the associated {@link CaseDetails} for the ID provided

     */
    @Retryable({FeignException.class, RuntimeException.class})
    public List<CaseDetails> getAllUserCases(String authorization) {
        UserDetails userDetails = idamClient.getUserDetails(authorization);

        List<CaseDetails> scotlandCases = ccdApiClient.searchForCitizen(
            authorization, authTokenGenerator.generate(),
            userDetails.getId(), JURISDICTION_ID, SCOTLAND_CASE_TYPE, Collections.emptyMap());

        List<CaseDetails> englandCases = ccdApiClient.searchForCitizen(
            authorization, authTokenGenerator.generate(),
            userDetails.getId(), JURISDICTION_ID, ENGLAND_CASE_TYPE, Collections.emptyMap());

        return Stream.of(scotlandCases, englandCases).flatMap(Collection::stream).collect(toList());
    }


    /**
     * Given a caseID, this will retrieve the correct {@link CaseDetails}.
     *
     * @param authorization is used to find the {@link UserDetails} for request
     * @param caseRequest  case data for request
     * @return the associated {@link CaseDetails} if the case is created

     */
    @Retryable({FeignException.class, RuntimeException.class})
    public CaseDetails createCase(String authorization,
                                  CaseRequest caseRequest) {
        log.info("Creating Case");
        EmployeeObjectMapper employeeObjectMapper = new EmployeeObjectMapper();
        Et1CaseData data = employeeObjectMapper.getEmploymentCaseData(caseRequest.getCaseData());
        String s2sToken = authTokenGenerator.generate();
        log.info("Generated s2s");
        UserDetails userDetails = idamClient.getUserDetails(authorization);
        log.info("User Id: " + userDetails.getId());
        log.info("Roles : " + userDetails.getRoles());

        var caseType = getCaseType(caseRequest);
        var eventType = CaseEvent.INITIATE_CASE_DRAFT;
        var ccdCase = ccdApiClient.startForCitizen(
            authorization,
            s2sToken,
            userDetails.getId(),
            JURISDICTION_ID,
            caseType,
            eventType.name()
        );

        log.info("Started Case: " + ccdCase.getEventId());

        CaseDataContent caseDataContent = CaseDataContent.builder()
            .event(Event.builder().id(eventType.name()).build())
            .eventToken(ccdCase.getToken())
            .data(data)
            .build();
        return ccdApiClient.submitForCitizen(
            authorization,
            s2sToken,
            userDetails.getId(),
            JURISDICTION_ID,
            caseType,
            true,
            caseDataContent
        );
    }

    private String getCaseType(CaseRequest caseRequest) {
        try {
            return getCaseTypeId(
                postcodeToOfficeService.getTribunalOfficeFromPostcode(caseRequest.getPostCode())
                    .orElse(DEFAULT_TRIBUNAL_OFFICE).getOfficeName());
        } catch (InvalidPostcodeException e) {
            log.info("Failed to find tribunal office : {} ", e.getMessage());
            return getCaseTypeId(DEFAULT_TRIBUNAL_OFFICE.getOfficeName());
        }
    }

    public CaseDetails updateCase(String authorization,
                                  CaseRequest caseRequest) {
        return triggerEvent(authorization, caseRequest.getCaseId(), CaseEvent.UPDATE_CASE_DRAFT,
                            caseRequest.getCaseTypeId(), caseRequest.getCaseData());
    }

    public CaseDetails submitCase(String authorization,
                                  CaseRequest caseRequest) {
        return triggerEvent(authorization, caseRequest.getCaseId(), CaseEvent.SUBMIT_CASE_DRAFT,
                            caseRequest.getCaseTypeId(), caseRequest.getCaseData());
    }

    /**
     * Given a caseId, triggers update events for the case.
     *
     * @param authorization is used to seek the {@link UserDetails} for request
     * @param caseId used to retrive get case details
     * @param caseType is used to determine if the case is for ET_EnglandWales or ET_Scotland
     * @param eventName is used to determine INITIATE_CASE_DRAFT or UPDATE_CASE_DRAFT
     * @param caseData is used to provide the {@link Et1CaseData} in json format
     * @return the associated {@link CaseData} if the case is updated
     */
    public CaseDetails triggerEvent(String authorization, String caseId, String caseType,
                                 CaseEvent eventName, Map<String, Object> caseData) {
        return triggerEvent(authorization, caseId, eventName, caseType, caseData);
    }

    /**
     * Given a caseId, initialization of trigger event to start and submit update for case.
     *
     * @param authorization is used to seek the {@link UserDetails} for request
     * @param caseId used to retrive get case details
     * @param caseType is used to determine if the case is for ET_EnglandWales or ET_Scotland
     * @param eventName is used to determine INITIATE_CASE_DRAFT or UPDATE_CASE_DRAFT
     * @param caseData is used to provide the {@link Et1CaseData} in json format
     * @return the associated {@link CaseData} if the case is updated
     */
    public CaseDetails triggerEvent(String authorization, String caseId, CaseEvent eventName,
                                 String caseType, Map<String, Object> caseData) {
        ObjectMapper objectMapper = new ObjectMapper();
        CaseDetailsConverter caseDetailsConverter = new CaseDetailsConverter(objectMapper);
        EmployeeObjectMapper employeeObjectMapper = new EmployeeObjectMapper();
        StartEventResponse startEventResponse = startUpdate(authorization, caseId, caseType, eventName);
        return submitUpdate(authorization, caseId,
                            caseDetailsConverter.caseDataContent(startEventResponse,
                            employeeObjectMapper.getEmploymentCaseData(caseData)),
                            caseType);
    }

    /**
     * Given a caseId, start update for the case.
     *
     * @param authorization is used to seek the {@link UserDetails} for request
     * @param caseId used to retrive get case details
     * @param caseType is used to determine if the case is for ET_EnglandWales or ET_Scotland
     * @param eventName is used to determine INITIATE_CASE_DRAFT or UPDATE_CASE_DRAFT
     * @return startEventResponse associated case details updated
     */
    public StartEventResponse startUpdate(String authorization, String caseId,
                                          String caseType, CaseEvent eventName) {
        String s2sToken = authTokenGenerator.generate();
        UserDetails userDetails = idamClient.getUserDetails(authorization);

        return ccdApiClient.startEventForCitizen(
            authorization,
            s2sToken,
            userDetails.getId(),
            JURISDICTION_ID,
            caseType,
            caseId,
            eventName.name()
        );
    }

    /**
     * Given a caseId, submit update for the case.
     *
     * @param authorization is used to seek the {@link UserDetails} for request
     * @param caseId used to retrive get case details
     * @param caseDataContent provides overall content of the case
     * @param caseType is used to determine if the case is for ET_EnglandWales or ET_Scotland
     */
    public CaseDetails submitUpdate(String authorization, String caseId,
                                    CaseDataContent caseDataContent, String caseType) {
        UserDetails userDetails = idamClient.getUserDetails(authorization);
        String s2sToken = authTokenGenerator.generate();

        return ccdApiClient.submitEventForCitizen(
            authorization,
            s2sToken,
            userDetails.getId(),
            JURISDICTION_ID,
            caseType,
            caseId,
            true,
            caseDataContent
        );
    }
}
