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
import uk.gov.dwp.regex.InvalidPostcodeException;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;
import uk.gov.hmcts.et.common.model.ccd.Et1CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.JurCodesTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.JurCodesType;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.ccd.client.model.SearchResult;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants;
import uk.gov.hmcts.reform.et.syaapi.constants.JurisdictionCodesConstants;
import uk.gov.hmcts.reform.et.syaapi.enums.CaseEvent;
import uk.gov.hmcts.reform.et.syaapi.helper.CaseDetailsConverter;
import uk.gov.hmcts.reform.et.syaapi.helper.EmployeeObjectMapper;
import uk.gov.hmcts.reform.et.syaapi.helper.JurisdictionCodesMapper;
import uk.gov.hmcts.reform.et.syaapi.models.CaseRequest;
import uk.gov.hmcts.reform.et.syaapi.utils.ResourceLoader;
import uk.gov.hmcts.reform.et.syaapi.utils.ResourceUtil;
import uk.gov.hmcts.reform.et.syaapi.utils.TestConstants;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.et.syaapi.enums.CaseEvent.UPDATE_CASE_DRAFT;
import static uk.gov.hmcts.reform.et.syaapi.utils.TestConstants.TEST_SERVICE_AUTH_TOKEN;

@EqualsAndHashCode
@ExtendWith(MockitoExtension.class)
@SuppressWarnings({"PMD.TooManyMethods", "PMD.TooManyFields", "PMD.ExcessiveImports"})
class CaseServiceTest {
    private static final String CASE_TYPE = "ET_Scotland";
    private static final String CASE_ID = "TEST_CASE_ID";
    private static final String USER_ID = "TEST_USER_ID";
    private static final String JURISDICTION_ID = "EMPLOYMENT";
    private static final String USER_EMAIL = "test@gmail.com";
    private static final String USER_FORENAME = "Joe";
    private static final String USER_SURNAME = "Bloggs";

    @Mock
    private PostcodeToOfficeService postcodeToOfficeService;

    @Mock
    private JurisdictionCodesMapper jurisdictionCodesMapper;

    private final CaseDetails expectedDetails = ResourceLoader.fromString(
        "responses/caseDetails.json",
        CaseDetails.class
    );

    private final List<CaseDetails> requestCaseDataListEngland = ResourceLoader.fromStringToList(
        "responses/caseDetailsEngland.json",
        CaseDetails.class
    );

    private final List<CaseDetails> requestCaseDataListScotland = ResourceLoader.fromStringToList(
        "responses/caseDetailsScotland.json",
        CaseDetails.class
    );

    private final List<CaseDetails> expectedCaseDataListCombined = ResourceLoader.fromStringToList(
        "responses/caseDetailsCombined.json",
        CaseDetails.class
    );

    private final StartEventResponse startEventResponse = ResourceLoader.fromString(
        "responses/startEventResponse.json",
        StartEventResponse.class
    );
    private final String requestCaseData = ResourceUtil.resourceAsString(
        "requests/caseData.json"
    );
    private final Et1CaseData caseData = ResourceLoader.fromString(
        "requests/caseData.json",
        Et1CaseData.class
    );

    private final List<CaseDetails> requestCaseDataList = ResourceLoader.fromStringToList(
        "responses/caseDetailsList.json",
        CaseDetails.class
    );

    private final CaseRequest caseDataWithClaimTypes = ResourceLoader.fromString(
        "requests/caseDataWithClaimTypes.json",
        CaseRequest.class
    );

    @Mock
    private AuthTokenGenerator authTokenGenerator;
    @Mock
    private CoreCaseDataApi ccdApiClient;
    @Mock
    private IdamClient idamClient;

    @InjectMocks
    private CaseService caseService;
    @Mock
    private CaseDetailsConverter caseDetailsConverter;

    CaseServiceTest() throws IOException {
        // Default constructor
    }

    @Test
    void shouldGetUserCase() {
        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(ccdApiClient.getCase(
            TEST_SERVICE_AUTH_TOKEN,
            TEST_SERVICE_AUTH_TOKEN,
            CASE_ID
        )).thenReturn(expectedDetails);

        CaseRequest caseRequest = CaseRequest.builder()
            .caseId(CASE_ID).build();

        CaseDetails caseDetails = caseService.getUserCase(TEST_SERVICE_AUTH_TOKEN, caseRequest);

        assertEquals(expectedDetails, caseDetails);
    }

    @Test
    void shouldGetAllUserCases() {
        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(idamClient.getUserDetails(TEST_SERVICE_AUTH_TOKEN)).thenReturn(new UserDetails(
            USER_ID,
            USER_EMAIL,
            USER_FORENAME,
            USER_SURNAME,
            null
        ));
        when(ccdApiClient.searchForCitizen(
            TEST_SERVICE_AUTH_TOKEN,
            TEST_SERVICE_AUTH_TOKEN,
            USER_ID,
            JURISDICTION_ID,
            EtSyaConstants.SCOTLAND_CASE_TYPE,
            Collections.emptyMap()
        )).thenReturn(requestCaseDataList);

        List<CaseDetails> caseDetails = caseService.getAllUserCases(TEST_SERVICE_AUTH_TOKEN);

        assertEquals(requestCaseDataList, caseDetails);
    }

    @Test
    void shouldGetAllUserCasesDifferentCaseType() {
        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(idamClient.getUserDetails(TEST_SERVICE_AUTH_TOKEN)).thenReturn(new UserDetails(
            USER_ID,
            USER_EMAIL,
            USER_FORENAME,
            USER_SURNAME,
            null
        ));
        when(ccdApiClient.searchForCitizen(
            TEST_SERVICE_AUTH_TOKEN,
            TEST_SERVICE_AUTH_TOKEN,
            USER_ID,
            JURISDICTION_ID,
            EtSyaConstants.SCOTLAND_CASE_TYPE,
            Collections.emptyMap()
        )).thenReturn(requestCaseDataListScotland);

        when(ccdApiClient.searchForCitizen(
            TEST_SERVICE_AUTH_TOKEN,
            TEST_SERVICE_AUTH_TOKEN,
            USER_ID,
            JURISDICTION_ID,
            EtSyaConstants.ENGLAND_CASE_TYPE,
            Collections.emptyMap()
        )).thenReturn(requestCaseDataListEngland);

        List<CaseDetails> caseDetails = caseService.getAllUserCases(TEST_SERVICE_AUTH_TOKEN);

        assertThat(expectedCaseDataListCombined).hasSize(caseDetails.size()).hasSameElementsAs(caseDetails);
    }

    @Test
    void shouldCreateNewDraftCaseInCcd() throws InvalidPostcodeException {
        CaseDataContent caseDataContent = CaseDataContent.builder()
            .event(Event.builder().id(EtSyaConstants.DRAFT_EVENT_TYPE).build())
            .eventToken(startEventResponse.getToken())
            .data(caseData)
            .build();
        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(idamClient.getUserDetails(TEST_SERVICE_AUTH_TOKEN)).thenReturn(new UserDetails(
            USER_ID,
            USER_EMAIL,
            USER_FORENAME,
            USER_SURNAME,
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
            startEventResponse);

        when(ccdApiClient.submitForCitizen(
            TEST_SERVICE_AUTH_TOKEN,
            TEST_SERVICE_AUTH_TOKEN,
            USER_ID,
            EtSyaConstants.JURISDICTION_ID,
            EtSyaConstants.SCOTLAND_CASE_TYPE,
            true,
            caseDataContent
        )).thenReturn(expectedDetails);

        when(postcodeToOfficeService.getTribunalOfficeFromPostcode(any()))
            .thenReturn(Optional.of(TribunalOffice.ABERDEEN));

        CaseRequest caseRequest = CaseRequest.builder()
            .postCode("AB10 1AH")
            .caseData(new HashMap<>())
            .build();

        CaseDetails caseDetails = caseService.createCase(
            TEST_SERVICE_AUTH_TOKEN,
            caseRequest
        );

        assertEquals(expectedDetails, caseDetails);
    }

    @Test
    void shouldStartUpdateCaseInCcd() {
        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(idamClient.getUserDetails(TEST_SERVICE_AUTH_TOKEN)).thenReturn(new UserDetails(
            USER_ID,
            USER_EMAIL,
            USER_FORENAME,
            USER_SURNAME,
            null
        ));

        when(ccdApiClient.startEventForCitizen(
            TEST_SERVICE_AUTH_TOKEN,
            TEST_SERVICE_AUTH_TOKEN,
            USER_ID,
            EtSyaConstants.JURISDICTION_ID,
            EtSyaConstants.SCOTLAND_CASE_TYPE,
            CASE_ID,
            TestConstants.UPDATE_CASE_DRAFT
        )).thenReturn(
            startEventResponse);

        StartEventResponse eventResponse = caseService.startUpdate(
            TEST_SERVICE_AUTH_TOKEN,
            CASE_ID,
            EtSyaConstants.SCOTLAND_CASE_TYPE,
            UPDATE_CASE_DRAFT
        );

        assertEquals(eventResponse.getCaseDetails().getCaseTypeId(), CASE_TYPE);
    }

    @Test
    void shouldSubmitUpdateCaseInCcd() {
        CaseDataContent caseDataContent = CaseDataContent.builder()
            .event(Event.builder().id(String.valueOf(UPDATE_CASE_DRAFT)).build())
            .eventToken(startEventResponse.getToken())
            .data(caseData)
            .build();

        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(idamClient.getUserDetails(TEST_SERVICE_AUTH_TOKEN)).thenReturn(new UserDetails(
            USER_ID,
            USER_EMAIL,
            USER_FORENAME,
            USER_SURNAME,
            null
        ));

        when(ccdApiClient.submitEventForCitizen(
            TEST_SERVICE_AUTH_TOKEN,
            TEST_SERVICE_AUTH_TOKEN,
            USER_ID,
            EtSyaConstants.JURISDICTION_ID,
            EtSyaConstants.SCOTLAND_CASE_TYPE,
            CASE_ID,
            true,
            caseDataContent
        )).thenReturn(expectedDetails);

        CaseDetails caseDetails = caseService.submitUpdate(
            TEST_SERVICE_AUTH_TOKEN,
            CASE_ID,
            caseDataContent,
            EtSyaConstants.SCOTLAND_CASE_TYPE
        );

        assertEquals(caseDetails, expectedDetails);
    }

    @Test
    void shouldLastModifiedCasesIdWhenCaseFoundThenCaseId() {
        LocalDateTime requestDateTime =
            LocalDateTime.parse("2022-09-01T12:34:00", DateTimeFormatter.ISO_LOCAL_DATE_TIME);

        SearchResult englandWalesSearchResult = SearchResult.builder()
            .total(1)
            .cases(requestCaseDataListEngland)
            .build();
        SearchResult scotlandSearchResult = SearchResult.builder()
            .total(2)
            .cases(requestCaseDataListScotland)
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
            .query(boolQueryBuilder)
            .toString();
    }

    @Test
    void shouldReturnCaseData() throws IOException {
        List<String> caseIds = List.of("1646225213651598", "1646225213651533", "1646225213651512");
        SearchResult englandWalesSearchResult = SearchResult.builder()
            .total(1)
            .cases(requestCaseDataListEngland)
            .build();
        SearchResult scotlandSearchResult = SearchResult.builder()
            .total(2)
            .cases(requestCaseDataListScotland)
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
        assertThat(caseDetailsList).isEqualTo(expectedCaseDataListCombined);
    }

    @Test
    void shouldReturnCaseDataNoCasesFound() throws IOException {
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

    @Test
    void shouldInvokeCaseEnrichmentWithJurCodesInSubmitEvent() {
        List<JurCodesTypeItem> expectedItems = mockJurCodesTypeItems();
        EmployeeObjectMapper employeeObjectMapper = new EmployeeObjectMapper();
        Et1CaseData et1CaseData = employeeObjectMapper.getEmploymentCaseData(caseDataWithClaimTypes.getCaseData());
        et1CaseData.setJurCodesCollection(expectedItems);

        CaseDataContent expectedEnrichedData = CaseDataContent.builder()
            .event(Event.builder().id("initiateCaseDraft").build())
            .eventToken(startEventResponse.getToken())
            .data(et1CaseData)
            .build();

        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(idamClient.getUserDetails(TEST_SERVICE_AUTH_TOKEN)).thenReturn(new UserDetails(
            USER_ID,
            USER_EMAIL,
            USER_FORENAME,
            USER_SURNAME,
            null
        ));

        when(ccdApiClient.startEventForCitizen(
            TEST_SERVICE_AUTH_TOKEN,
            TEST_SERVICE_AUTH_TOKEN,
            USER_ID,
            EtSyaConstants.JURISDICTION_ID,
            EtSyaConstants.ENGLAND_CASE_TYPE,
            CASE_ID,
            TestConstants.SUBMIT_CASE_DRAFT
        )).thenReturn(
            startEventResponse);

        when(ccdApiClient.submitEventForCitizen(
            TEST_SERVICE_AUTH_TOKEN,
            TEST_SERVICE_AUTH_TOKEN,
            USER_ID,
            EtSyaConstants.JURISDICTION_ID,
            EtSyaConstants.ENGLAND_CASE_TYPE,
            CASE_ID,
            true,
            expectedEnrichedData
        )).thenReturn(expectedDetails);

        when(jurisdictionCodesMapper.mapToJurCodes(any())).thenReturn(expectedItems);

        caseService.triggerEvent(
            TEST_SERVICE_AUTH_TOKEN,
            CASE_ID,
            CaseEvent.valueOf("SUBMIT_CASE_DRAFT"),
            EtSyaConstants.ENGLAND_CASE_TYPE,
            caseDataWithClaimTypes.getCaseData()
        );

        verify(ccdApiClient).submitEventForCitizen(TEST_SERVICE_AUTH_TOKEN,
                                                   TEST_SERVICE_AUTH_TOKEN,
                                                   USER_ID,
                                                   EtSyaConstants.JURISDICTION_ID,
                                                   EtSyaConstants.ENGLAND_CASE_TYPE,
                                                   CASE_ID,
                                                   true,
                                                   expectedEnrichedData);
    }

    public List<JurCodesTypeItem> mockJurCodesTypeItems() {
        JurCodesTypeItem item = new JurCodesTypeItem();
        JurCodesType type = new JurCodesType();
        type.setJuridictionCodesList(JurisdictionCodesConstants.BOC);
        item.setValue(type);
        return List.of(item);
    }

}
