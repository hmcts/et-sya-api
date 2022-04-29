package uk.gov.hmcts.reform.et.syaapi.service;

import lombok.EqualsAndHashCode;
import org.elasticsearch.index.query.QueryBuilders;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.Et1CaseData;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.ccd.client.model.SearchResult;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.et.syaapi.client.CcdApiClient;
import uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants;
import uk.gov.hmcts.reform.et.syaapi.helper.CaseDetailsConverter;
import uk.gov.hmcts.reform.et.syaapi.search.Query;
import uk.gov.hmcts.reform.et.syaapi.utils.ResourceLoader;
import uk.gov.hmcts.reform.et.syaapi.utils.ResourceUtil;
import uk.gov.hmcts.reform.et.syaapi.utils.TestConstants;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.et.syaapi.enums.CaseEvent.UPDATE_CASE_DRAFT;
import static uk.gov.hmcts.reform.et.syaapi.utils.TestConstants.TEST_SERVICE_AUTH_TOKEN;

@EqualsAndHashCode
@ExtendWith(MockitoExtension.class)
class CaseServiceTest {
    private static final String CASE_TYPE = "ET_Scotland";
    private static final String CASE_ID = "TEST_CASE_ID";
    private static final String USER_ID = "TEST_USER_ID";
    private static final String JURISDICTION_ID = "EMPLOYMENT";

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
    private final Et1CaseData caseData = ResourceLoader.fromString(
        "requests/caseData.json",
        Et1CaseData.class
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
    @Mock
    private CaseDetailsConverter caseDetailsConverter;


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
    void shouldGetCaseDetailsbyUser() {
        String searchString = "{\"match_all\": {}}";
        Query query = new Query(QueryBuilders.wrapperQuery(searchString), 0);

        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(idamClient.getUserDetails(TEST_SERVICE_AUTH_TOKEN)).thenReturn(new UserDetails(
            USER_ID,
            "test@gmail.com",
            "Joe",
            "Bloggs",
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


        List<CaseDetails> expectedDataList = caseService.getCaseDataByUser(TEST_SERVICE_AUTH_TOKEN,
                                  EtSyaConstants.SCOTLAND_CASE_TYPE);
        assertEquals(requestCaseDataList, expectedDataList);
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
            USER_ID,
            "test@gmail.com",
            "Joe",
            "Bloggs",
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

        CaseDetails caseDetails = caseService.createCase(
            TEST_SERVICE_AUTH_TOKEN,
            EtSyaConstants.SCOTLAND_CASE_TYPE,
            EtSyaConstants.DRAFT_EVENT_TYPE,
            requestCaseData
        );

        assertEquals(expectedDetails, caseDetails);
    }

    @Test
    void shouldStartUpdateCaseInCcd() {
        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(idamClient.getUserDetails(TEST_SERVICE_AUTH_TOKEN)).thenReturn(new UserDetails(
            USER_ID,
            "test@gmail.com",
            "Joe",
            "Bloggs",
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
            "test@gmail.com",
            "Joe",
            "Bloggs",
            null
        ));

        when(ccdApiClient.startEventForCitizen(
            TEST_SERVICE_AUTH_TOKEN,
            TEST_SERVICE_AUTH_TOKEN,
            USER_ID,
            EtSyaConstants.JURISDICTION_ID,
            EtSyaConstants.SCOTLAND_CASE_TYPE,
            CASE_ID,
            "UPDATE_CASE_DRAFT"
        )).thenReturn(
            startEventResponse);

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

        StartEventResponse eventResponse = caseService.startUpdate(
            TEST_SERVICE_AUTH_TOKEN,
            CASE_ID,
            EtSyaConstants.SCOTLAND_CASE_TYPE,
            UPDATE_CASE_DRAFT
        );

        CaseData caseData = caseService.submitUpdate(
            TEST_SERVICE_AUTH_TOKEN,
            CASE_ID,
            caseDataContent,
            EtSyaConstants.SCOTLAND_CASE_TYPE, caseDetailsConverter
        );

        assertNull(caseData);
        assertEquals(eventResponse.getCaseDetails().getJurisdiction(), "EMPLOYMENT");
    }
}
