package uk.gov.hmcts.reform.et.syaapi.service;

import lombok.EqualsAndHashCode;
import org.elasticsearch.index.query.QueryBuilders;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.et.syaapi.client.CcdApiClient;
import uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants;
import uk.gov.hmcts.reform.et.syaapi.models.EmploymentCaseData;
import uk.gov.hmcts.reform.et.syaapi.search.Query;
import uk.gov.hmcts.reform.et.syaapi.utils.ResourceLoader;
import uk.gov.hmcts.reform.et.syaapi.utils.ResourceUtil;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.et.syaapi.utils.TestConstants.TEST_SERVICE_AUTH_TOKEN;

@EqualsAndHashCode
@ExtendWith(MockitoExtension.class)
class CaseServiceTest {
    private static final String CASE_TYPE = "ET_Scotland";
    private static final String USER_ID = "1234";

    private final CaseDetails expectedDetails = ResourceLoader.fromString(
        "responses/caseDetails.json",
        CaseDetails.class
    );
    private final StartEventResponse startEventResponse = ResourceLoader.fromString(
        "responses/startEventResponse.json",
        StartEventResponse.class
    );
    private final String requestCaseData = ResourceUtil.resourceAsString(
        "requests/caseData.json"
    );
    private final EmploymentCaseData caseData = ResourceLoader.fromString(
        "requests/caseData.json",
        EmploymentCaseData.class
    );

    private final List<CaseDetails> requestCaseDataList = ResourceLoader.fromStringToList(
        "responses/caseDetailsList.json",
        CaseDetails.class
    );

    @Mock
    private AuthTokenGenerator authTokenGenerator;
    @Mock
    private CcdApiClient ccdApiClient;
    @Mock
    private IdamClient idamClient;
    @InjectMocks
    private CaseService caseService;

    CaseServiceTest() throws IOException {
        // Default constructor
    }

    @Test
    void shouldGetCaseDetailsReturnsData() {
        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(ccdApiClient.getCase(
            TEST_SERVICE_AUTH_TOKEN,
            TEST_SERVICE_AUTH_TOKEN,
            EtSyaConstants.SCOTLAND_CASE_TYPE
        )).thenReturn(expectedDetails);

        CaseDetails caseDetails = caseService.getCaseData(TEST_SERVICE_AUTH_TOKEN, EtSyaConstants.SCOTLAND_CASE_TYPE);

        assertEquals(expectedDetails, caseDetails);
    }

    @Test
    void shouldCreateNewDraftCaseInCcd() {
        CaseDataContent caseDataContent = CaseDataContent.builder()
            .event(Event.builder().id(EtSyaConstants.DRAFT_EVENT_TYPE).build())
            .eventToken(startEventResponse.getToken())
            .data(caseData)
            .build();
        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(idamClient.getUserDetails(TEST_SERVICE_AUTH_TOKEN)).thenReturn(new UserDetails(
            "12",
            "test@gmail.com",
            "Joe",
            "Bloggs",
            null
        ));
        when(ccdApiClient.startForCaseworker(
            TEST_SERVICE_AUTH_TOKEN,
            TEST_SERVICE_AUTH_TOKEN,
            "12",
            EtSyaConstants.JURISDICTION_ID,
            EtSyaConstants.SCOTLAND_CASE_TYPE,
            EtSyaConstants.DRAFT_EVENT_TYPE
        )).thenReturn(
            startEventResponse);
        when(ccdApiClient.submitForCaseworker(
            TEST_SERVICE_AUTH_TOKEN,
            TEST_SERVICE_AUTH_TOKEN,
            "12",
            EtSyaConstants.JURISDICTION_ID,
            EtSyaConstants.SCOTLAND_CASE_TYPE,
            true,
            caseDataContent
        )).thenReturn(expectedDetails);

        CaseDetails caseDetails = caseService.createCase(
            TEST_SERVICE_AUTH_TOKEN,
            EtSyaConstants.SCOTLAND_CASE_TYPE,
            EtSyaConstants.DRAFT_EVENT_TYPE,
            requestCaseData
        );

        assertEquals(expectedDetails, caseDetails);
    }

    @Test
    void shouldGetCaseDetailsForUser() {
        // given
        String searchString = "{\"match_all\": {}}";
        Query query = new Query(QueryBuilders.wrapperQuery(searchString), 0);

        when(idamClient.getUserDetails(TEST_SERVICE_AUTH_TOKEN)).thenReturn(UserDetails.builder().id(USER_ID).build());
        when(ccdApiClient.searchCases(TEST_SERVICE_AUTH_TOKEN,
                                     TEST_SERVICE_AUTH_TOKEN,
                                     EtSyaConstants.SCOTLAND_CASE_TYPE,
                                     query.toString()).getCases())
            .thenReturn(requestCaseDataList);
        List<CaseDetails> caseDetailsList = caseService.getCaseDataByUser(
            TEST_SERVICE_AUTH_TOKEN,
            CASE_TYPE, query.toString()
        );
        assertEquals(caseDetailsList, requestCaseDataList);

    }
}
