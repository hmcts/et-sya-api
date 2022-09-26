package uk.gov.hmcts.reform.et.syaapi.service;

import lombok.EqualsAndHashCode;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.index.query.TermsQueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SearchResult;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants;
import uk.gov.hmcts.reform.et.syaapi.model.TestData;
import uk.gov.hmcts.reform.et.syaapi.models.CaseRequest;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

import static  org.assertj.core.api.Assertions.assertThat;
import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.MAX_ES_SIZE;
import static uk.gov.hmcts.reform.et.syaapi.enums.CaseEvent.UPDATE_CASE_DRAFT;
import static uk.gov.hmcts.reform.et.syaapi.utils.TestConstants.TEST_SERVICE_AUTH_TOKEN;
import static uk.gov.hmcts.reform.et.syaapi.utils.TestConstants.USER_ID;

@EqualsAndHashCode
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("PMD.TooManyMethods")
class CaseServiceTest {

    @Mock
    private PostcodeToOfficeService postcodeToOfficeService;
    @Mock
    private AuthTokenGenerator authTokenGenerator;
    @Mock
    private CoreCaseDataApi ccdApiClient;
    @Mock
    private IdamClient idamClient;
    @InjectMocks
    private CaseService caseService;
    private final TestData testData;


    CaseServiceTest() {
        testData = new TestData();
    }

    @Test
    void shouldGetUserCase() {
        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(ccdApiClient.getCase(
            TEST_SERVICE_AUTH_TOKEN,
            TEST_SERVICE_AUTH_TOKEN,
            testData.getCaseRequest().getCaseId()
        )).thenReturn(testData.getExpectedDetails());

        CaseRequest caseRequest = CaseRequest.builder()
            .caseId(testData.getCaseRequest().getCaseId()).build();

        CaseDetails caseDetails = caseService.getUserCase(TEST_SERVICE_AUTH_TOKEN, caseRequest);

        assertEquals(testData.getExpectedDetails(), caseDetails);
    }

    @Test
    void shouldGetAllUserCases() {
        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(idamClient.getUserDetails(TEST_SERVICE_AUTH_TOKEN)).thenReturn(new UserDetails(
            USER_ID,
            testData.getCaseData().getClaimantType().getClaimantEmailAddress(),
            testData.getCaseData().getClaimantIndType().getClaimantFirstNames(),
            testData.getCaseData().getClaimantIndType().getClaimantLastName(),
            null
        ));
        when(ccdApiClient.searchForCitizen(
            TEST_SERVICE_AUTH_TOKEN,
            TEST_SERVICE_AUTH_TOKEN,
            USER_ID,
            testData.getExpectedDetails().getJurisdiction(),
            EtSyaConstants.SCOTLAND_CASE_TYPE,
            Collections.emptyMap()
        )).thenReturn(testData.getRequestCaseDataList());

        List<CaseDetails> caseDetails = caseService.getAllUserCases(TEST_SERVICE_AUTH_TOKEN);

        assertEquals(testData.getRequestCaseDataList(), caseDetails);
    }

    @Test
    void shouldGetAllUserCasesDifferentCaseType() {
        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(idamClient.getUserDetails(TEST_SERVICE_AUTH_TOKEN)).thenReturn(new UserDetails(
            USER_ID,
            testData.getCaseData().getClaimantType().getClaimantEmailAddress(),
            testData.getCaseData().getClaimantIndType().getClaimantFirstNames(),
            testData.getCaseData().getClaimantIndType().getClaimantLastName(),
            null
        ));
        when(ccdApiClient.searchForCitizen(
            TEST_SERVICE_AUTH_TOKEN,
            TEST_SERVICE_AUTH_TOKEN,
            USER_ID,
            testData.getExpectedDetails().getJurisdiction(),
            EtSyaConstants.SCOTLAND_CASE_TYPE,
            Collections.emptyMap()
        )).thenReturn(testData.getRequestCaseDataListScotland());

        when(ccdApiClient.searchForCitizen(
            TEST_SERVICE_AUTH_TOKEN,
            TEST_SERVICE_AUTH_TOKEN,
            USER_ID,
            testData.getExpectedDetails().getJurisdiction(),
            EtSyaConstants.ENGLAND_CASE_TYPE,
            Collections.emptyMap()
        )).thenReturn(testData.getRequestCaseDataListEngland());

        List<CaseDetails> caseDetails = caseService.getAllUserCases(TEST_SERVICE_AUTH_TOKEN);

        assertThat(testData.getExpectedCaseDataListCombined())
            .hasSize(caseDetails.size()).hasSameElementsAs(caseDetails);
    }

    @Test
    void shouldCreateNewDraftCaseInCcd() {

        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(idamClient.getUserDetails(TEST_SERVICE_AUTH_TOKEN)).thenReturn(new UserDetails(
            USER_ID,
            testData.getCaseData().getClaimantType().getClaimantEmailAddress(),
            testData.getCaseData().getClaimantIndType().getClaimantFirstNames(),
            testData.getCaseData().getClaimantIndType().getClaimantLastName(),
            null
        ));
        when(ccdApiClient.startForCitizen(
            TEST_SERVICE_AUTH_TOKEN,
            TEST_SERVICE_AUTH_TOKEN,
            USER_ID,
            EtSyaConstants.JURISDICTION_ID,
            EtSyaConstants.SCOTLAND_CASE_TYPE,
            EtSyaConstants.DRAFT_EVENT_TYPE
        )).thenReturn(
            testData.getStartEventResponse());

        when(ccdApiClient.submitForCitizen(
            eq(TEST_SERVICE_AUTH_TOKEN),
            eq(TEST_SERVICE_AUTH_TOKEN),
             eq(USER_ID),
            eq(EtSyaConstants.JURISDICTION_ID),
            eq(EtSyaConstants.SCOTLAND_CASE_TYPE),
            eq(true),
            any(CaseDataContent.class)
        )).thenReturn(testData.getExpectedDetails());

        CaseRequest caseRequest = testData.getCaseRequest();

        CaseDetails caseDetails = caseService.createCase(
            TEST_SERVICE_AUTH_TOKEN,
            caseRequest
        );

        assertEquals(testData.getExpectedDetails(), caseDetails);
    }

    @Test
    void shouldStartUpdateCaseInCcd() {
        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(idamClient.getUserDetails(TEST_SERVICE_AUTH_TOKEN)).thenReturn(new UserDetails(
            USER_ID,
            testData.getCaseData().getClaimantType().getClaimantEmailAddress(),
            testData.getCaseData().getClaimantIndType().getClaimantFirstNames(),
            testData.getCaseData().getClaimantIndType().getClaimantLastName(),
            null
        ));

        when(ccdApiClient.startEventForCitizen(
            TEST_SERVICE_AUTH_TOKEN,
            TEST_SERVICE_AUTH_TOKEN,
            USER_ID,
            EtSyaConstants.JURISDICTION_ID,
            EtSyaConstants.SCOTLAND_CASE_TYPE,
            testData.getCaseRequest().getCaseId(),
            String.valueOf(UPDATE_CASE_DRAFT)
        )).thenReturn(
            testData.getStartEventResponse());

        StartEventResponse eventResponse = caseService.startUpdate(
            TEST_SERVICE_AUTH_TOKEN,
            testData.getCaseRequest().getCaseId(),
            EtSyaConstants.SCOTLAND_CASE_TYPE,
            UPDATE_CASE_DRAFT
        );

        assertEquals(EtSyaConstants.SCOTLAND_CASE_TYPE, eventResponse.getCaseDetails().getCaseTypeId());
    }

    @Test
    void shouldSubmitUpdateCaseInCcd() {
        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(idamClient.getUserDetails(TEST_SERVICE_AUTH_TOKEN)).thenReturn(new UserDetails(
            USER_ID,
            testData.getCaseData().getClaimantType().getClaimantEmailAddress(),
            testData.getCaseData().getClaimantIndType().getClaimantFirstNames(),
            testData.getCaseData().getClaimantIndType().getClaimantLastName(),
            null
        ));
        when(ccdApiClient.submitEventForCitizen(
            TEST_SERVICE_AUTH_TOKEN,
            TEST_SERVICE_AUTH_TOKEN,
            USER_ID,
            EtSyaConstants.JURISDICTION_ID,
            EtSyaConstants.SCOTLAND_CASE_TYPE,
            testData.getCaseRequest().getCaseId(),
            true,
            testData.getUpdateCaseDataContent()
        )).thenReturn(testData.getExpectedDetails());

        CaseDetails caseDetails = caseService.submitUpdate(
            TEST_SERVICE_AUTH_TOKEN,
            testData.getCaseRequest().getCaseId(),
            testData.getUpdateCaseDataContent(),
            EtSyaConstants.SCOTLAND_CASE_TYPE
        );

        assertEquals(caseDetails, testData.getExpectedDetails());
    }

    @Test
    void shouldLastModifiedCasesIdWhenCaseFoundThenCaseId() {
        LocalDateTime requestDateTime =
            LocalDateTime.parse("2022-09-01T12:34:00", DateTimeFormatter.ISO_LOCAL_DATE_TIME);

        SearchResult englandWalesSearchResult = SearchResult.builder()
            .total(1)
            .cases(testData.getRequestCaseDataListEngland())
            .build();
        SearchResult scotlandSearchResult = SearchResult.builder()
            .total(2)
            .cases(testData.getRequestCaseDataListScotland())
            .build();

        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(ccdApiClient.searchCases(
            TEST_SERVICE_AUTH_TOKEN,
            TEST_SERVICE_AUTH_TOKEN,
            EtSyaConstants.ENGLAND_CASE_TYPE,
            generateCaseDataEsQueryWithDate(requestDateTime)
        )).thenReturn(englandWalesSearchResult);
        when(ccdApiClient.searchCases(
            TEST_SERVICE_AUTH_TOKEN,
            TEST_SERVICE_AUTH_TOKEN,
            EtSyaConstants.SCOTLAND_CASE_TYPE,
            generateCaseDataEsQueryWithDate(requestDateTime)
        )).thenReturn(scotlandSearchResult);

        assertThat(caseService.getLastModifiedCasesId(TEST_SERVICE_AUTH_TOKEN, requestDateTime))
            .hasSize(3)
            .isEqualTo(List.of(1_646_225_213_651_598L, 1_646_225_213_651_533L, 1_646_225_213_651_512L));
    }

    @Test
    void shouldLastModifiedCasesIdWhenNoCaseFoundThenEmpty() {
        LocalDateTime requestDateTime =
            LocalDateTime.parse("2022-09-01T12:34:00", DateTimeFormatter.ISO_LOCAL_DATE_TIME);

        SearchResult englandWalesSearchResult = SearchResult.builder()
            .total(0)
            .cases(null)
            .build();
        SearchResult scotlandSearchResult = SearchResult.builder()
            .total(0)
            .cases(null)
            .build();

        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(ccdApiClient.searchCases(
            TEST_SERVICE_AUTH_TOKEN,
            TEST_SERVICE_AUTH_TOKEN,
            EtSyaConstants.ENGLAND_CASE_TYPE,
            generateCaseDataEsQueryWithDate(requestDateTime)
        )).thenReturn(englandWalesSearchResult);
        when(ccdApiClient.searchCases(
            TEST_SERVICE_AUTH_TOKEN,
            TEST_SERVICE_AUTH_TOKEN,
            EtSyaConstants.SCOTLAND_CASE_TYPE,
            generateCaseDataEsQueryWithDate(requestDateTime)
        )).thenReturn(scotlandSearchResult);

        assertThat(caseService.getLastModifiedCasesId(TEST_SERVICE_AUTH_TOKEN, requestDateTime))
            .isEmpty();
    }

    private String generateCaseDataEsQueryWithDate(LocalDateTime requestDateTime) {
        BoolQueryBuilder boolQueryBuilder = boolQuery()
            .filter(new RangeQueryBuilder("last_modified").gte(requestDateTime));
        return new SearchSourceBuilder()
            .size(MAX_ES_SIZE)
            .query(boolQueryBuilder)
            .toString();
    }

    @Test
    void shouldReturnCaseData() {
        List<String> caseIds = List.of("1646225213651598", "1646225213651533", "1646225213651512");
        SearchResult englandWalesSearchResult = SearchResult.builder()
            .total(1)
            .cases(testData.getRequestCaseDataListEngland())
            .build();
        SearchResult scotlandSearchResult = SearchResult.builder()
            .total(2)
            .cases(testData.getRequestCaseDataListScotland())
            .build();

        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(ccdApiClient.searchCases(
            TEST_SERVICE_AUTH_TOKEN,
            TEST_SERVICE_AUTH_TOKEN,
            EtSyaConstants.ENGLAND_CASE_TYPE, generateCaseDataEsQuery(caseIds)
        )).thenReturn(englandWalesSearchResult);
        when(ccdApiClient.searchCases(
            TEST_SERVICE_AUTH_TOKEN,
            TEST_SERVICE_AUTH_TOKEN,
            EtSyaConstants.SCOTLAND_CASE_TYPE, generateCaseDataEsQuery(caseIds)
        )).thenReturn(scotlandSearchResult);

        List<CaseDetails> caseDetailsList = caseService.getCaseData(TEST_SERVICE_AUTH_TOKEN, caseIds);
        assertThat(caseDetailsList).hasSize(3);
        assertThat(caseDetailsList).isEqualTo(testData.getExpectedCaseDataListCombined());
    }

    @Test
    void shouldReturnCaseDataNoCasesFound() {
        List<String> caseIds = List.of("1646225213651598", "1646225213651533");
        SearchResult englandWalesSearchResult = SearchResult.builder()
            .total(0)
            .cases(null)
            .build();
        SearchResult scotlandSearchResult = SearchResult.builder()
            .total(0)
            .cases(null)
            .build();

        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(ccdApiClient.searchCases(
            TEST_SERVICE_AUTH_TOKEN,
            TEST_SERVICE_AUTH_TOKEN,
            EtSyaConstants.ENGLAND_CASE_TYPE, generateCaseDataEsQuery(caseIds)
        )).thenReturn(englandWalesSearchResult);
        when(ccdApiClient.searchCases(
            TEST_SERVICE_AUTH_TOKEN,
            TEST_SERVICE_AUTH_TOKEN,
            EtSyaConstants.SCOTLAND_CASE_TYPE, generateCaseDataEsQuery(caseIds)
        )).thenReturn(scotlandSearchResult);

        List<CaseDetails> caseDetailsList = caseService.getCaseData(TEST_SERVICE_AUTH_TOKEN, caseIds);
        assertThat(caseDetailsList).isEmpty();
    }

    private String generateCaseDataEsQuery(List<String> caseIds) {
        BoolQueryBuilder boolQueryBuilder = boolQuery()
            .filter(new TermsQueryBuilder("reference.keyword", caseIds));
        return new SearchSourceBuilder()
            .query(boolQueryBuilder)
            .toString();
    }
}
