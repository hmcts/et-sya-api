package uk.gov.hmcts.reform.et.syaapi.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.Et3Request;
import uk.gov.hmcts.et.common.model.ccd.types.RespondentSumType;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.et.syaapi.constants.CaseDetailsLinks;
import uk.gov.hmcts.reform.et.syaapi.constants.ResponseConstants;
import uk.gov.hmcts.reform.et.syaapi.constants.ResponseHubLinks;
import uk.gov.hmcts.reform.et.syaapi.exception.ET3Exception;
import uk.gov.hmcts.reform.et.syaapi.exception.ManageCaseRoleException;
import uk.gov.hmcts.reform.et.syaapi.helper.EmployeeObjectMapper;
import uk.gov.hmcts.reform.et.syaapi.service.utils.ResponseUtil;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Stream;

import static org.springframework.beans.BeanUtils.copyProperties;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.ENGLAND_CASE_TYPE;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.JURISDICTION_ID;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.SCOTLAND_CASE_TYPE;
import static uk.gov.hmcts.reform.et.syaapi.constants.ManageCaseRoleConstants.EXCEPTION_CASE_DETAILS_NOT_FOUND;
import static uk.gov.hmcts.reform.et.syaapi.constants.ManageCaseRoleConstants.EXCEPTION_CASE_DETAILS_NOT_FOUND_EMPTY_PARAMETERS;
import static uk.gov.hmcts.reform.et.syaapi.enums.CaseEvent.UPDATE_CASE_SUBMITTED;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.ResponseUtil.findRespondentSumTypeItemByIdamId;

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

    /**
     * Finds case by its submission reference.
     * @param submissionReference case data id, or submission reference id of the case
     * @return case details for the given submission reference and case type id.
     */
    public CaseDetails findCaseBySubmissionReference(String submissionReference) {
        if (StringUtils.isBlank(submissionReference)) {
            throw new RuntimeException(EXCEPTION_CASE_DETAILS_NOT_FOUND_EMPTY_PARAMETERS);
        }
        CaseDetails caseDetails = ccdApi.getCase(adminUserService.getAdminUserToken(),
                                                 authTokenGenerator.generate(),
                                                 submissionReference);
        if (ObjectUtils.isNotEmpty(caseDetails)) {
            return caseDetails;
        }
        throw new RuntimeException(String.format(EXCEPTION_CASE_DETAILS_NOT_FOUND, submissionReference));
    }

    /**
     * Updates case details with the new values for ET3 case assignment or ET3 updates.
     * @param authorisation authorisation token of the user
     * @param caseDetails case details which is updated with the given ET3 updates for respondent
     */
    public CaseDetails updateSubmittedCaseWithCaseDetails(String authorisation, CaseDetails caseDetails) {
        return caseService.triggerEvent(authorisation,
                                        caseDetails.getId().toString(),
                                        UPDATE_CASE_SUBMITTED,
                                        caseDetails.getCaseTypeId(),
                                        caseDetails.getData());
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
        log.info("******** get all user cases For ET3 user id: {}", userInfo.getUid());
        List<CaseDetails> englandCases = ccdApi.searchForCitizen(
            authorization,
            authTokenGenerator.generate(),
            userInfo.getUid(),
            JURISDICTION_ID,
            ENGLAND_CASE_TYPE,
            new HashMap<>());
        List<CaseDetails> scotlandCases = ccdApi.searchForCitizen(
            authorization,
            authTokenGenerator.generate(),
            userInfo.getUid(),
            JURISDICTION_ID,
            SCOTLAND_CASE_TYPE,
            new HashMap<>());
        log.info("ALL USER CASES, gatAllUserCasesForET3: {}", Stream.of(scotlandCases, englandCases)
            .flatMap(Collection::stream).toList());
        return Stream.of(scotlandCases, englandCases)
            .flatMap(Collection::stream).toList();
    }

    @Retryable
    public CaseDetails modifyEt3Data(String authorisation, Et3Request et3Request) {
        ResponseUtil.checkModifyEt3DataParameters(authorisation, et3Request);
        CaseDetails caseDetails = findCaseBySubmissionReference(et3Request.getCaseSubmissionReference());
        if (ObjectUtils.isEmpty(caseDetails)) {
            throw new ET3Exception(new Exception(ResponseConstants.EXCEPTION_UNABLE_TO_FIND_CASE_DETAILS));
        }
        CaseData caseData = EmployeeObjectMapper.mapRequestCaseDataToCaseData(caseDetails.getData());
        RespondentSumType selectedRespondent =
            findRespondentSumTypeItemByIdamId(caseData, et3Request.getRespondent().getIdamId()).getValue();
        ResponseHubLinks.setResponseHubLinkStatus(et3Request.getRespondent(),
                                                  et3Request.getResponseHubLinksSectionId(),
                                                  et3Request.getResponseHubLinksSectionStatus());
        CaseDetailsLinks.setCaseDetailsLinkStatus(et3Request.getRespondent(),
                                                  et3Request.getCaseDetailsLinksSectionId(),
                                                  et3Request.getCaseDetailsLinksSectionStatus());
        copyProperties(et3Request.getRespondent(), selectedRespondent);
        caseDetails.setData(EmployeeObjectMapper.mapCaseDataToLinkedHashMap(caseData));
        return updateSubmittedCaseWithCaseDetails(authorisation, caseDetails);
    }
}
