package uk.gov.hmcts.reform.et.syaapi.search;

import org.apache.commons.lang3.StringUtils;
import uk.gov.hmcts.reform.et.syaapi.models.FindCaseForRoleModificationRequest;

@SuppressWarnings({"PMD.AvoidDuplicateLiterals"})
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

    private ElasticSearchQueryBuilder() {
        // Access through static methods
    }

    /**
     * Generates query to search case by caseSubmissionReference, respondentName, claimantFirstNames,
     * and claimantLastName. This query is used to check if the respondent's entered data for self
     * assignment exists or not.
     * Compares respondent name with the fields, respondent name and organisation name. Respondent name field in the
     * database json object's value is always equals the respondent name when type of respondent is individual and
     * always equals to organisation name when type of respondent is organisation. But it may also have the name as
     * combination of respondent first name, space, respondent last name. That is why it is added as or statement to
     * both organisation name and respondent name.
     * @param findCaseForRoleModificationRequest is the parameter object which has caseSubmissionReference,
     *                                           respondentName, claimantFirstNames and claimantLastName
     * @return the string value of the elastic search query
     */
    public static String buildByFindCaseForRoleModificationRequest(
        FindCaseForRoleModificationRequest findCaseForRoleModificationRequest
    ) {

        // This code is not being used as we have problem with spring boot - elastic search query builder.
        // This is a bug and needs to be resolved with the new release of elastic search
        // https://github.com/elastic/elasticsearch/issues/109165
        // Respondent Queries
        /*
            BoolQueryBuilder boolQueryForRespondentOrganisationName = boolQuery().filter(
                new TermQueryBuilder(FIELD_NAME_RESPONDENT_ORGANISATION,
                                                       findCaseForRoleModificationRequest
                                                       .getRespondentName()).caseInsensitive(true));
            BoolQueryBuilder boolQueryForRespondentName = boolQuery().filter(
                new TermQueryBuilder(FIELD_NAME_RESPONDENT_NAME,
                                      findCaseForRoleModificationRequest.getRespondentName()).caseInsensitive(true));
            // Respondent name is the field that always has one of the names. If it is an organisation it has the
            // organisation name or if it is an individual has the individual name. Just added that field as an
            // or to both organisation and respondent names.
            BoolQueryBuilder boolQueryForRespondent = boolQuery().filter(
                new TermQueryBuilder(FIELD_NAME_RESPONDENT,
                findCaseForRoleModificationRequest.getRespondentName()).caseInsensitive(true));


            // Claimant Queries
            BoolQueryBuilder boolQueryForClaimantFirstNames = boolQuery().filter(
                new TermQueryBuilder(FIELD_NAME_CLAIMANT_FIRST_NAMES,
                                      findCaseForRoleModificationRequest.getClaimantFirstNames()));
            BoolQueryBuilder boolQueryForClaimantLastName = boolQuery().filter(
                new TermQueryBuilder(FIELD_NAME_CLAIMANT_LAST_NAME,
                                      findCaseForRoleModificationRequest.getClaimantLastName()));
            BoolQueryBuilder boolQueryForClaimantFullName = boolQuery().filter(
                new TermQueryBuilder(FIELD_NAME_CLAIMANT_FULL_NAME,
                                      findCaseForRoleModificationRequest.getClaimantFirstNames()
                                          + StringUtils.SPACE
                                          + findCaseForRoleModificationRequest
                                          .getClaimantLastName()).caseInsensitive(true));
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
                .size(1)
                .query(boolQueryBuilder).toString();
            */
        /* return "{\"size\":1,\"query\":{\"bool\":{\"must\":[{\"match\":{\"" + FIELD_NAME_SUBMISSION_REFERENCE + "\":"
            + "{\"query\":\"" + findCaseForRoleModificationRequest.getCaseSubmissionReference()
            + "\"}}}],\"filter\":[{\"bool\":{\"should\":[{\"bool\":"
            + "{\"filter\":[{\"match\":{\"" + FIELD_NAME_RESPONDENT_ORGANISATION + "\":"
            + "{\"query\":\"" + findCaseForRoleModificationRequest.getRespondentName() + "\"}}}],\"boost\":1.0}},"
            + "{\"bool\":{\"filter\":[{\"match\":{\"" + FIELD_NAME_RESPONDENT_NAME + "\""
            + ":{\"query\":\"" + findCaseForRoleModificationRequest.getRespondentName() + "\"}}}],"
            + "\"boost\":1.0}},{\"bool\":{\"filter\":[{\"match\":{\"" + FIELD_NAME_RESPONDENT + "\":{\"query\":"
            + "\"" + findCaseForRoleModificationRequest.getRespondentName() + "\"}}}],\"boost\":1.0}}],\"boost\":1.0}},"
            + "{\"bool\":{\"should\":[{\"bool\":{\"must\":[{\"bool\":{\"filter\":[{\"match\":"
            + "{\"" + FIELD_NAME_CLAIMANT_FIRST_NAMES + "\""
            + ":{\"query\":\"" + findCaseForRoleModificationRequest.getClaimantFirstNames()
            + "\"}}}],\"boost\":1.0}},{\"bool\":{\"filter\":[{\"match\""
            + ":{\"" + FIELD_NAME_CLAIMANT_LAST_NAME + "\":{\"query\":\""
            + findCaseForRoleModificationRequest.getClaimantLastName() + "\"}}}],\"boost\""
            + ":1.0}}],\"boost\":1.0}},{\"bool\":{\"filter\":[{\"match\":{\"" + FIELD_NAME_CLAIMANT_FULL_NAME
            + "\":{\"query\":\"" + findCaseForRoleModificationRequest.getClaimantFirstNames()
            + StringUtils.SPACE + findCaseForRoleModificationRequest.getClaimantLastName()
            + "\"}}}],\"boost\":1.0}}],\"boost\":1.0}}],\"boost\":1.0}}}";*/
        return "{\"size\":1,\"query\":{\"bool\":{\"must\":[{\"match\":{\"" + FIELD_NAME_SUBMISSION_REFERENCE + "\":"
            + "{\"query\":\"" + findCaseForRoleModificationRequest.getCaseSubmissionReference()
            + "\"}}}],\"filter\":[{\"bool\":{\"should\":[{\"bool\":{\"filter\":[{\"term\":{\""
            + FIELD_NAME_RESPONDENT_ORGANISATION + "\":{\"value\":\""
            + findCaseForRoleModificationRequest.getRespondentName() + "\",\"case_insensitive\":true}}}],"
            + "\"boost\":1.0}},{\"bool\":{\"filter\":[{\"term\":{\"" + FIELD_NAME_RESPONDENT_NAME
            + "\":{\"value\":\"" + findCaseForRoleModificationRequest.getRespondentName() + "\""
            + ",\"case_insensitive\":true}}}],\"boost\":1.0}},{\"bool\":{\"filter\":[{\"term\""
            + ":{\"" + FIELD_NAME_RESPONDENT + "\":{\"value\":\""
            + findCaseForRoleModificationRequest.getRespondentName() + "\",\"case_insensitive\":true}}}],"
            + "\"boost\":1.0}}],\"boost\":1.0}},{\"bool\":{\"should\":[{\"bool\":{\"must\":[{\"bool\":{\"filter\":"
            + "[{\"term\":{\"" + FIELD_NAME_CLAIMANT_FIRST_NAMES + "\":{\"value\":\""
            + findCaseForRoleModificationRequest.getClaimantFirstNames() + "\"}}}],"
            + "\"boost\":1.0}},{\"bool\":{\"filter\":[{\"term\":{\"" + FIELD_NAME_CLAIMANT_LAST_NAME + "\""
            + ":{\"value\":\"" + findCaseForRoleModificationRequest.getClaimantLastName() + "\"}}}],\"boost\":1.0}}],"
            + "\"boost\":1.0}},{\"bool\":{\"filter\":"
            + "[{\"term\":{\"" + FIELD_NAME_CLAIMANT_FULL_NAME
            + "\":{\"value\":\"" + findCaseForRoleModificationRequest.getClaimantFirstNames()
            + StringUtils.SPACE + findCaseForRoleModificationRequest.getClaimantLastName()
            + "\",\"case_insensitive\":true}}}],"
            + "\"boost\":1.0}}],\"boost\":1.0}}],\"boost\":1.0}}}";
    }

    /**
     * Generates query to search case by caseSubmissionReference, claimantFirstNames,
     * and claimantLastName. This query is used to check if the claimant's entered data for self
     * assignment exists or not.
     * Compares claimant name with the fields, claimant first names, claimant last name, and full name.
     * @param findCaseForRoleModificationRequest is the parameter object which has caseSubmissionReference,
     *                                           claimantFirstNames and claimantLastName
     * @return the string value of the elastic search query
     */
    public static String buildByFindCaseForRoleModificationRequestClaimant(
        FindCaseForRoleModificationRequest findCaseForRoleModificationRequest
    ) {
        return "{\"size\":1,\"query\":{\"bool\":{\"must\":[{\"match\":{\"" + FIELD_NAME_SUBMISSION_REFERENCE + "\":"
            + "{\"query\":\"" + findCaseForRoleModificationRequest.getCaseSubmissionReference()
            + "\"}}}],\"filter\":[{\"bool\":{\"should\":[{\"bool\":{\"filter\":[{\"term\":{\""
            + FIELD_NAME_CLAIMANT_FIRST_NAMES
            + "\":{\"value\":\"" + findCaseForRoleModificationRequest.getClaimantFirstNames() + "\"}}}],"
            + "\"boost\":1.0}},{\"bool\":{\"filter\":[{\"term\":{\"" + FIELD_NAME_CLAIMANT_LAST_NAME + "\""
            + ":{\"value\":\"" + findCaseForRoleModificationRequest.getClaimantLastName() + "\"}}}],\"boost\":1.0}},"
            + "{\"bool\":{\"filter\":[{\"term\":{\"" + FIELD_NAME_CLAIMANT_FULL_NAME
            + "\":{\"value\":\"" + findCaseForRoleModificationRequest.getClaimantFirstNames()
            + StringUtils.SPACE + findCaseForRoleModificationRequest.getClaimantLastName()
            + "\",\"case_insensitive\":true}}}],\"boost\":1.0}}],\"boost\":1.0}}],\"boost\":1.0}}}";
    }

    /**
     * This query is used to get the specific case with the entered case submission reference to find the case.
     * that will be assigned to the respondent.
     * @param submissionReference submissionReference of the case
     * @return the string value of the elastic search query
     */
    public static String buildBySubmissionReference(String submissionReference) {
        /*
        BoolQueryBuilder boolQueryBuilder = boolQuery()
            .must(new MatchQueryBuilder(FIELD_NAME_SUBMISSION_REFERENCE, submissionReference));
        return new SearchSourceBuilder()
            .size(ES_SIZE)
            .query(boolQueryBuilder).toString();
            */
        return "{\"size\":1,\"query\":{\"bool\":{\"must\":[{\"match\":{\"" + FIELD_NAME_SUBMISSION_REFERENCE + "\":"
            + "{\"query\":\"" + submissionReference + "\"}}}],\"boost\":1.0}}}";
    }

    /**
     * This query is used to get the specific case with the entered ethos case reference to find the case.
     * @param ethosCaseReference ethosCaseReference of the case
     * @return the string value of the elastic search query
     */
    public static String buildByEthosCaseReference(String ethosCaseReference) {
        return """
            {
              "size": 1,
              "query": {
                "bool": {
                  "must": [
                    {
                      "match": {
                        "data.ethosCaseReference.keyword": {
                          "query": "%s"
                        }
                      }
                    }
                  ],
                  "must_not": [
                    {
                      "match": {
                        "data.migratedFromEcm": {
                          "query": "Yes"
                        }
                      }
                    }
                  ]
                }
              }
            }
            """.formatted(ethosCaseReference);
    }
}
