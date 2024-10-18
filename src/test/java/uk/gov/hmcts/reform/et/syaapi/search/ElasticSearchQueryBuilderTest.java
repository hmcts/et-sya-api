package uk.gov.hmcts.reform.et.syaapi.search;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.et.syaapi.models.FindCaseForRoleModificationRequest;

import static org.assertj.core.api.Assertions.assertThat;

class ElasticSearchQueryBuilderTest {

    private static final String CASE_SUBMISSION_REFERENCE = "case_submission_reference";
    private static final String RESPONDENT_NAME = "respondent_name";
    private static final String CLAIMANT_FIRST_NAMES = "claimant_first_names";
    private static final String CLAIMANT_LAST_NAME = "claimant_last_name";
    private static final String EXPECTED_QUERY_BY_ROLE_MODIFICATION_REQUEST =
        "{\"size\":1,\"query\":{\"bool\":{\"must\":[{\"match\":{\"reference.keyword\":{\"query\":"
            + "\"case_submission_reference\"}}}],\"filter\":[{\"bool\":{\"should\":[{\"bool\":{\"filter\":"
            + "[{\"match\":{\"data.respondentCollection.value.respondentOrganisation.keyword\":{\"query\":"
            + "\"respondent_name\"}}}],\"boost\":1.0}},{\"bool\":{\"filter\":[{\"match\""
            + ":{\"data.respondentCollection.value.respondent_name.keyword\":{\"query\":\"respondent_name\"}}}],"
            + "\"boost\":1.0}},{\"bool\":{\"filter\":[{\"match\":{\"data.respondent.keyword\":{\"query\":"
            + "\"respondent_name\"}}}],\"boost\":1.0}}],\"boost\":1.0}},{\"bool\":{\"should\":[{\"bool\":"
            + "{\"must\":[{\"bool\":{\"filter\":[{\"match\":{\"data.claimantIndType.claimant_first_names.keyword\":"
            + "{\"query\":\"claimant_first_names\"}}}],\"boost\":1.0}},{\"bool\":{\"filter\":[{\"match\":"
            + "{\"data.claimantIndType.claimant_last_name.keyword\":{\"query\":\"claimant_last_name\"}}}],\"boost\":"
            + "1.0}}],\"boost\":1.0}},{\"bool\":{\"filter\":[{\"match\":{\"data.claimant.keyword\":{\"query\":"
            + "\"claimant_first_names claimant_last_name\"}}}],\"boost\":1.0}}],\"boost\":1.0}}],\"boost\":1.0}}}";
    private static final String EXPECTED_QUERY_BY_SUBMISSION_REFERENCE = "{\"size\":1,\"query\":{\"bool\":{\"must\":"
        + "[{\"match\":{\"reference.keyword\":{\"query\":\"case_submission_reference\"}}}],\"boost\":1.0}}}";

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
}
