package uk.gov.hmcts.reform.et.syaapi.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import static uk.gov.hmcts.reform.et.syaapi.constants.ManageCaseRoleConstants.EXCEPTION_CASE_DETAILS_NOT_FOUND;
import static uk.gov.hmcts.reform.et.syaapi.constants.ManageCaseRoleConstants.EXCEPTION_CASE_DETAILS_NOT_FOUND_EMPTY_PARAMETERS;
import static uk.gov.hmcts.reform.et.syaapi.enums.CaseEvent.UPDATE_CASE_SUBMITTED;

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
    public void updateSubmittedCaseWithCaseDetails(String authorisation, CaseDetails caseDetails) {
        caseService.triggerEvent(authorisation,
                                 caseDetails.getId().toString(),
                                 UPDATE_CASE_SUBMITTED,
                                 caseDetails.getCaseTypeId(),
                                 caseDetails.getData());
    }

}
