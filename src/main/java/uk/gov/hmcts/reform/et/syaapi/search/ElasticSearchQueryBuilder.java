package uk.gov.hmcts.reform.et.syaapi.search;

import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.TermsQueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import uk.gov.hmcts.reform.et.syaapi.models.FindCaseForRoleModificationRequest;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;

public final class ElasticSearchQueryBuilder {

    private static final String FIELD_NAME_RESPONDENT_ORGANISATION
        = "data.respondentCollection.value.respondentOrganisation";
    private static final String FIELD_NAME_RESPONDENT_NAME
        = "data.respondentCollection.value.respondent_name";
    private static final String FIELD_NAME_RESPONDENT = "data.respondent";
    private static final String FIELD_NAME_SUBMISSION_REFERENCE = "reference.keyword";
    private static final String FIELD_NAME_CLAIMANT_FIRST_NAMES = "data.claimantIndType.claimant_first_names";
    private static final String FIELD_NAME_CLAIMANT_LAST_NAME = "data.claimantIndType.claimant_last_name";
    private static final String FIELD_NAME_CLAIMANT_FULL_NAME = "data.claimant";
    private static final int ES_SIZE = 1;

    private ElasticSearchQueryBuilder() {
        // Access through static methods
    }

    public static String buildElasticSearchQueryForRoleModification(
        FindCaseForRoleModificationRequest findCaseForRoleModificationRequest
    ) {
        // Respondent Queries
        BoolQueryBuilder boolQueryForRespondentOrganisationName = boolQuery().filter(
            new TermsQueryBuilder(FIELD_NAME_RESPONDENT_ORGANISATION,
                                  findCaseForRoleModificationRequest.getRespondentName()));
        BoolQueryBuilder boolQueryForRespondentName = boolQuery().filter(
            new TermsQueryBuilder(FIELD_NAME_RESPONDENT_NAME, findCaseForRoleModificationRequest.getRespondentName()));
        BoolQueryBuilder boolQueryForRespondent = boolQuery().filter(
            new TermsQueryBuilder(FIELD_NAME_RESPONDENT, findCaseForRoleModificationRequest.getRespondentName()));
        // Claimant Queries
        BoolQueryBuilder boolQueryForClaimantFirstNames = boolQuery().filter(
            new MatchQueryBuilder(FIELD_NAME_CLAIMANT_FIRST_NAMES,
                                  findCaseForRoleModificationRequest.getClaimantFirstNames()));
        BoolQueryBuilder boolQueryForClaimantLastName = boolQuery().filter(
            new MatchQueryBuilder(FIELD_NAME_CLAIMANT_LAST_NAME,
                                  findCaseForRoleModificationRequest.getClaimantLastName()));
        BoolQueryBuilder boolQueryForClaimantFullName = boolQuery().filter(
            new MatchQueryBuilder(FIELD_NAME_CLAIMANT_FULL_NAME,
                                  findCaseForRoleModificationRequest.getClaimantFirstNames()
                                      + StringUtils.SPACE
                                      + findCaseForRoleModificationRequest.getClaimantLastName()));
        // Total Query
        BoolQueryBuilder boolQueryBuilder = boolQuery()
            .must(new MatchQueryBuilder(FIELD_NAME_SUBMISSION_REFERENCE,
                                        findCaseForRoleModificationRequest.getCaseSubmissionReference()))
            .filter(boolQuery()
                        .should(boolQueryForRespondentOrganisationName)
                        .should(boolQueryForRespondentName)
                        .should(boolQueryForRespondent))
            .filter(boolQuery()
                        .must(boolQueryForClaimantFirstNames)
                        .must(boolQueryForClaimantLastName)
                        .should(boolQueryForClaimantFullName));
        return new SearchSourceBuilder()
            .size(ES_SIZE)
            .query(boolQueryBuilder).toString();
    }
}
