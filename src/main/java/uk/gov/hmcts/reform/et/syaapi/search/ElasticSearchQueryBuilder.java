package uk.gov.hmcts.reform.et.syaapi.search;

import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import uk.gov.hmcts.reform.et.syaapi.models.FindCaseForRoleModificationRequest;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;

public final class ElasticSearchQueryBuilder {

    private static final String FIELD_NAME_RESPONDENT_ORGANISATION
        = "data.respondentCollection.value.respondentOrganisation.keyword";
    private static final String FIELD_NAME_RESPONDENT_NAME
        = "data.respondentCollection.value.respondent_name.keyword";
    private static final String FIELD_NAME_RESPONDENT = "data.respondent.keyword";
    private static final String FIELD_NAME_SUBMISSION_REFERENCE = "reference.keyword";
    private static final String FIELD_NAME_CLAIMANT_FIRST_NAMES = "data.claimantIndType.claimant_first_names.keyword";
    private static final String FIELD_NAME_CLAIMANT_LAST_NAME = "data.claimantIndType.claimant_last_name.keyword";
    private static final String FIELD_NAME_CLAIMANT_FULL_NAME = "data.claimant.keyword";
    private static final int ES_SIZE = 1;

    private ElasticSearchQueryBuilder() {
        // Access through static methods
    }

    /**
     * Generates query to search case by caseSubmissionReference, respondentName, claimantFirstNames,
     * and claimantLastName. This query is used to check if the respondent's entered data for self
     * assignment exists or not.
     * @param findCaseForRoleModificationRequest is the parameter object which has caseSubmissionReference,
     *                                           respondentName, claimantFirstNames and claimantLastName
     * @return the string value of the elastic search query
     */
    public static String buildByFindCaseForRoleModificationRequest(
        FindCaseForRoleModificationRequest findCaseForRoleModificationRequest
    ) {
        // Respondent Queries
        BoolQueryBuilder boolQueryForRespondentOrganisationName = boolQuery().filter(
            new MatchQueryBuilder(FIELD_NAME_RESPONDENT_ORGANISATION,
                                  findCaseForRoleModificationRequest.getRespondentName()));
        BoolQueryBuilder boolQueryForRespondentName = boolQuery().filter(
            new MatchQueryBuilder(FIELD_NAME_RESPONDENT_NAME, findCaseForRoleModificationRequest.getRespondentName()));
        BoolQueryBuilder boolQueryForRespondent = boolQuery().filter(
            new MatchQueryBuilder(FIELD_NAME_RESPONDENT, findCaseForRoleModificationRequest.getRespondentName()));


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
                        .should(boolQuery()
                                    .must(boolQueryForClaimantFirstNames)
                                    .must(boolQueryForClaimantLastName))
                        .should(boolQueryForClaimantFullName));
        return new SearchSourceBuilder()
            .size(ES_SIZE)
            .query(boolQueryBuilder).toString();
    }

    /**
     * This query is used to get the specific case with the entered case submission reference to find the case.
     * that will be assigned to the respondent.
     * @param submissionReference submissionReference of the case
     * @return the string value of the elastic search query
     */
    public static String buildBySubmissionReference(String submissionReference) {
        BoolQueryBuilder boolQueryBuilder = boolQuery()
            .must(new MatchQueryBuilder(FIELD_NAME_SUBMISSION_REFERENCE, submissionReference));
        return new SearchSourceBuilder()
            .size(ES_SIZE)
            .query(boolQueryBuilder).toString();
    }
}
