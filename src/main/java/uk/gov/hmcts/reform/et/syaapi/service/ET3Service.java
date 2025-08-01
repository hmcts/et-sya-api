package uk.gov.hmcts.reform.et.syaapi.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.Et1CaseData;
import uk.gov.hmcts.et.common.model.ccd.Et3Request;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants;
import uk.gov.hmcts.reform.et.syaapi.constants.ManageCaseRoleConstants;
import uk.gov.hmcts.reform.et.syaapi.enums.CaseEvent;
import uk.gov.hmcts.reform.et.syaapi.exception.ManageCaseRoleException;
import uk.gov.hmcts.reform.et.syaapi.helper.CaseDetailsConverter;
import uk.gov.hmcts.reform.et.syaapi.helper.EmployeeObjectMapper;
import uk.gov.hmcts.reform.et.syaapi.search.ElasticSearchQueryBuilder;
import uk.gov.hmcts.reform.et.syaapi.service.pdf.ET3FormService;
import uk.gov.hmcts.reform.et.syaapi.service.utils.HubLinksUtil;
import uk.gov.hmcts.reform.et.syaapi.service.utils.ResponseUtil;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.springframework.beans.BeanUtils.copyProperties;
import static uk.gov.hmcts.reform.et.syaapi.constants.ManageCaseRoleConstants.MODIFICATION_TYPE_SUBMIT;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.ResponseUtil.findSelectedRespondentByRespondentSumTypeItem;

/**
 * Provides services for ET3 Forms.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ET3Service {

    private final AdminUserService adminUserService;
    private final AuthTokenGenerator authTokenGenerator;
    private final CoreCaseDataApi ccdApi;
    private final IdamClient idamClient;
    private final CaseService caseService;
    private final ET3FormService et3FormService;
    private final NotificationService notificationService;
    private static final String FIELD_NAME_SUBMISSION_REFERENCE = "reference.keyword";
    private static final String FIELD_NAME_STATE = "state.keyword";
    private static final String STATE_VALUE_ACCEPTED = "Accepted";

    /**
     * Finds case by its submission reference.
     * @param submissionReference case data id, or submission reference id of the case
     * @return case details for the given submission reference and case type id.
     */
    public CaseDetails findCaseBySubmissionReference(String submissionReference) {
        if (isBlank(submissionReference)) {
            throw new RuntimeException(ManageCaseRoleConstants.EXCEPTION_CASE_DETAILS_NOT_FOUND_EMPTY_PARAMETERS);
        }
        CaseDetails caseDetails = ccdApi.getCase(adminUserService.getAdminUserToken(),
                                                 authTokenGenerator.generate(),
                                                 submissionReference);
        if (ObjectUtils.isNotEmpty(caseDetails)) {
            return caseDetails;
        }
        throw new RuntimeException(String.format(
            ManageCaseRoleConstants.EXCEPTION_CASE_DETAILS_NOT_FOUND, submissionReference));
    }

    public CaseDetails findCaseByIdAndAcceptedState(String id) {
        if (isBlank(id)) {
            throw new RuntimeException(ManageCaseRoleConstants.EXCEPTION_CASE_DETAILS_NOT_FOUND_EMPTY_PARAMETERS);
        }

        String elasticSearchQuery = "{\"size\":1,\"query\":{\"bool\":{\"must\":[{\"match\":{\""
            + FIELD_NAME_SUBMISSION_REFERENCE + "\":{\"query\":\"" + id + "\"}}},{\"match\""
            + ":{\"" + FIELD_NAME_STATE +  "\":{\"query\":\"" + STATE_VALUE_ACCEPTED + "\"}}}],\"boost\":1.0}}}";
        /*
         new SearchSourceBuilder()
        .size(1)
        .query(new BoolQueryBuilder()
        .must(new MatchQueryBuilder("reference.keyword", id))
        .must(new MatchQueryBuilder("state.keyword", "Accepted"))).toString();
        */
        return getCaseDetails(elasticSearchQuery);
    }

    private CaseDetails getCaseDetails(String elasticSearchQuery) {
        String adminUserToken = adminUserService.getAdminUserToken();
        CaseDetails englandCase = findCaseByCaseType(adminUserToken,
                                                     EtSyaConstants.ENGLAND_CASE_TYPE, elasticSearchQuery
        );
        if (ObjectUtils.isNotEmpty(englandCase)) {
            return englandCase;
        }

        CaseDetails scotlandCase = findCaseByCaseType(adminUserToken,
                                                      EtSyaConstants.SCOTLAND_CASE_TYPE, elasticSearchQuery
        );
        if (ObjectUtils.isNotEmpty(scotlandCase)) {
            return scotlandCase;
        }
        return null;
    }

    /**
     * Finds case by its ethos case reference.
     * @param ethosCaseReference case ethos reference of the case
     * @return case details for the given submission reference and case type id.
     */
    public CaseDetails findCaseByEthosCaseReference(String ethosCaseReference) {
        if (isBlank(ethosCaseReference)) {
            throw new RuntimeException(ManageCaseRoleConstants.EXCEPTION_CASE_DETAILS_NOT_FOUND_EMPTY_PARAMETERS);
        }
        String elasticSearchQuery = ElasticSearchQueryBuilder.buildByEthosCaseReference(ethosCaseReference);
        return getCaseDetails(elasticSearchQuery);
    }

    private CaseDetails findCaseByCaseType(String adminUserToken,
                                           String caseType,
                                           String elasticSearchQuery) {
        List<CaseDetails> caseDetailsList = Optional.ofNullable(ccdApi.searchCases(
            adminUserToken,
            authTokenGenerator.generate(),
            caseType,
            elasticSearchQuery
        ).getCases()).orElse(Collections.emptyList());
        return CollectionUtils.isNotEmpty(caseDetailsList)
            ? caseDetailsList.getFirst()
            : null;
    }

    /**
     * Updates case details with the new values for ET3 case assignment or ET3 updates.
     * @param authorisation authorisation token of the user
     * @param caseDetails case details which is updated with the given ET3 updates for respondent
     */
    public CaseDetails updateSubmittedCaseWithCaseDetailsForCaseAssignment(String authorisation,
                                                                           CaseDetails caseDetails,
                                                                           CaseEvent caseEvent) {
        return caseService.triggerEvent(authorisation,
                                        caseDetails.getId().toString(),
                                        caseEvent,
                                        caseDetails.getCaseTypeId(),
                                        caseDetails.getData());
    }

    /**
     * Updates case details with the new values for ET3 case assignment or ET3 updates.
     * @param authorisation authorisation token of the user
     * @param startEventResponse start event response is the response that has the latest updated case details data
     * @return CaseDetails that has the latest modified case data.
     */
    public CaseDetails updateSubmittedCaseWithCaseDetailsForET3FormUpdates(String authorisation,
                                                                           StartEventResponse startEventResponse) {
        return triggerEvent(authorisation,
                            startEventResponse.getCaseDetails().getId().toString(),
                            startEventResponse,
                            startEventResponse.getCaseDetails().getCaseTypeId(),
                            startEventResponse.getCaseDetails().getData());
    }

    /**
     * Given a caseId, initialization of trigger event to start and submit update for case.
     *
     * @param authorization      is used to seek the {@link UserInfo} for request
     * @param caseId             used to retrieve get case details
     * @param caseType           is used to determine if the case is for ET_EnglandWales or ET_Scotland
     * @param startEventResponse is used to determine INITIATE_CASE_DRAFT or UPDATE_CASE_DRAFT
     * @param caseData           is used to provide the {@link Et1CaseData} in json format
     * @return the associated {@link CaseData} if the case is updated
     */
    public CaseDetails triggerEvent(String authorization, String caseId, StartEventResponse startEventResponse,
                                    String caseType, Map<String, Object> caseData) {
        ObjectMapper objectMapper = new ObjectMapper();
        CaseDetailsConverter caseDetailsConverter = new CaseDetailsConverter(objectMapper);
        CaseData caseDataObject = EmployeeObjectMapper.convertCaseDataMapToCaseDataObject(caseData);
        return caseService.submitUpdate(
            authorization,
            caseId,
            caseDetailsConverter.et1ToCaseDataContent(startEventResponse, caseDataObject),
            caseType
        );
    }


    /**
     * Given a user derived from the authorisation token in the request,
     * this will get all cases {@link CaseDetails} for that user. This is implemented without elastic search
     * because after assigning a new case it needs to show all cases immediately and unfortunately with elastic search
     * It doesn't list at the first time. That is why implemented with direct DB search method on CCD.
     * NOTE: This method also works for creator citizen (et-sya) case list.
     *
     * @param authorization is used to get the {@link UserInfo} for the request
     * @return the associated {@link CaseDetails} list for the authorization code provided
     */
    // @Retryable({FeignException.class, RuntimeException.class}) --> No need to give exception classes as Retryable
    // covers all runtime exceptions.
    @Retryable
    protected List<CaseDetails> getAllUserCasesForET3(String authorization) {
        UserInfo userInfo = idamClient.getUserInfo(authorization);
        if (ObjectUtils.isEmpty(userInfo)) {
            log.info("Unable to get user info from idam for listing user cases");
            throw new ManageCaseRoleException(new Exception("Unable to get user info for listing user cases"));
        }
        List<CaseDetails> englandCases = ccdApi.searchForCitizen(
            authorization,
            authTokenGenerator.generate(),
            userInfo.getUid(),
            EtSyaConstants.JURISDICTION_ID,
            EtSyaConstants.ENGLAND_CASE_TYPE,
            new HashMap<>());
        List<CaseDetails> scotlandCases = ccdApi.searchForCitizen(
            authorization,
            authTokenGenerator.generate(),
            userInfo.getUid(),
            EtSyaConstants.JURISDICTION_ID,
            EtSyaConstants.SCOTLAND_CASE_TYPE,
            new HashMap<>());
        return Stream.of(scotlandCases, englandCases)
            .flatMap(Collection::stream).toList();
    }

    @Retryable
    public CaseDetails modifyEt3Data(String authorisation, Et3Request et3Request) {
        ResponseUtil.checkModifyEt3DataParameters(authorisation, et3Request);
        CaseEvent caseEvent = ResponseUtil.findCaseEventByRequestType(et3Request.getRequestType());
        StartEventResponse startEventResponse = caseService.startUpdate(authorisation,
                                                                        et3Request.getCaseSubmissionReference(),
                                                                        et3Request.getCaseTypeId(),
                                                                        caseEvent);
        CaseDetails caseDetails = ResponseUtil.getCaseDetailsByStartEventResponse(startEventResponse);
        CaseData caseData = EmployeeObjectMapper.convertCaseDataMapToCaseDataObject(caseDetails.getData());
        RespondentSumTypeItem selectedRespondent =
            findSelectedRespondentByRespondentSumTypeItem(caseData, et3Request.getRespondent());
        copyProperties(et3Request.getRespondent(), selectedRespondent);
        HubLinksUtil.setLinkStatuses(caseData, selectedRespondent.getValue(), et3Request);
        if (MODIFICATION_TYPE_SUBMIT.equals(et3Request.getRequestType())) {
            if (isBlank(selectedRespondent.getValue().getResponseRespondentEmail())) {
                UserInfo userInfo = idamClient.getUserInfo(authorisation);
                selectedRespondent.getValue().setResponseRespondentEmail(userInfo.getSub());
            }
            et3FormService.generateET3WelshAndEnglishForms(authorisation, caseData, selectedRespondent);
            ResponseUtil.setET3SubmitValues(selectedRespondent.getValue());
            notificationService.sendEt3ConfirmationEmail(selectedRespondent.getValue().getResponseRespondentEmail(),
                                                         caseData,
                                                         caseDetails.getId().toString());
        }
        caseDetails.setData(EmployeeObjectMapper.mapCaseDataToLinkedHashMap(caseData));
        return updateSubmittedCaseWithCaseDetailsForET3FormUpdates(authorisation, startEventResponse);
    }
}
