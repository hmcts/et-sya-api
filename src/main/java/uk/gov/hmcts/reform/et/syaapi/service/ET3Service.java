package uk.gov.hmcts.reform.et.syaapi.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.et.syaapi.search.ElasticSearchQueryBuilder;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static uk.gov.hmcts.reform.et.syaapi.constants.ManageCaseRoleConstants.EXCEPTION_CASE_DETAILS_NOT_FOUND;
import static uk.gov.hmcts.reform.et.syaapi.constants.ManageCaseRoleConstants.EXCEPTION_CASE_DETAILS_NOT_FOUND_EMPTY_PARAMETERS;
import static uk.gov.hmcts.reform.et.syaapi.constants.ManageCaseRoleConstants.FIRST_INDEX;

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

    public CaseDetails findCaseBySubmissionReferenceCaseTypeId(String submissionReference, String caseTypeId) {
        if (StringUtils.isBlank(submissionReference) || StringUtils.isBlank(caseTypeId)) {
            throw new RuntimeException(EXCEPTION_CASE_DETAILS_NOT_FOUND_EMPTY_PARAMETERS);
        }
        String adminUserToken = adminUserService.getAdminUserToken();
        String elasticSearchQuery = ElasticSearchQueryBuilder
            .buildBySubmissionReference(submissionReference);
        List<CaseDetails> caseDetailsList = Optional.ofNullable(ccdApi.searchCases(
            adminUserToken,
            authTokenGenerator.generate(),
            caseTypeId,
            elasticSearchQuery
        ).getCases()).orElse(Collections.emptyList());
        if (CollectionUtils.isNotEmpty(caseDetailsList)) {
            return caseDetailsList.get(FIRST_INDEX);
        }
        throw new RuntimeException(String.format(EXCEPTION_CASE_DETAILS_NOT_FOUND, submissionReference, caseTypeId));
    }
}
