package uk.gov.hmcts.reform.et.syaapi.search;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.et.syaapi.models.FindCaseForRoleModificationRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.EXPECTED_QUERY_BY_ETHOS_CASE_REFERENCE;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.EXPECTED_QUERY_BY_ROLE_MODIFICATION_REQUEST;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.EXPECTED_QUERY_BY_SUBMISSION_REFERENCE;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.TEST_ETHOS_CASE_REFERENCE;

class ElasticSearchQueryBuilderTest {

    private static final String CASE_SUBMISSION_REFERENCE = "case_submission_reference";
    private static final String RESPONDENT_NAME = "respondent_name";
    private static final String CLAIMANT_FIRST_NAMES = "claimant_first_names";
    private static final String CLAIMANT_LAST_NAME = "claimant_last_name";

    @Test
    void theBuildByFindCaseForRoleModificationRequest() {
        FindCaseForRoleModificationRequest findCaseForRoleModificationRequest =
            FindCaseForRoleModificationRequest
                .builder()
                .caseSubmissionReference(CASE_SUBMISSION_REFERENCE)
                .respondentName(RESPONDENT_NAME)
                .claimantFirstNames(CLAIMANT_FIRST_NAMES)
                .claimantLastName(CLAIMANT_LAST_NAME)
                .build();
        assertThat(ElasticSearchQueryBuilder.buildByFindCaseForRoleModificationRequest(
            findCaseForRoleModificationRequest)).isEqualTo(EXPECTED_QUERY_BY_ROLE_MODIFICATION_REQUEST);
    }

    @Test
    void theBuildBySubmissionReference() {
        assertThat(ElasticSearchQueryBuilder.buildBySubmissionReference(CASE_SUBMISSION_REFERENCE))
                       .isEqualTo(EXPECTED_QUERY_BY_SUBMISSION_REFERENCE);
    }

    @Test
    void theBuildByEthosCaseReference() {
        assertThat(ElasticSearchQueryBuilder.buildByEthosCaseReference(TEST_ETHOS_CASE_REFERENCE))
            .isEqualTo(EXPECTED_QUERY_BY_ETHOS_CASE_REFERENCE);
    }
}
