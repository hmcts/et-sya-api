package uk.gov.hmcts.reform.et.syaapi.service;

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
import uk.gov.hmcts.reform.et.syaapi.utils.ResourceLoader;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.util.List;

import static java.util.Collections.emptyMap;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.et.syaapi.utils.TestConstants.TEST_SERVICE_AUTH_TOKEN;

@ExtendWith(MockitoExtension.class)
class CaseServiceTest {
    private static final String CASE_ID = "ET_Scotland";
    private static final String EVENT_ID = "initiateCaseDraft";
    private static final String JURISDICTION_ID = "EMPLOYMENT";
    private static final String USER_ID = "12345";
    private final CaseDetails expectedDetails = ResourceLoader.fromString(
        "responses/caseDetails.json",
        CaseDetails.class
    );
    private final StartEventResponse startEventResponse = ResourceLoader.fromString(
        "responses/startEventResponse.json",
        StartEventResponse.class
    );

    private final List<CaseDetails> expectedDetailsList = ResourceLoader.fromStringToList(
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

    @Test
    void shouldGetCaseDetailsReturnsData() {
        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(ccdApiClient.getCase(
            TEST_SERVICE_AUTH_TOKEN,
            TEST_SERVICE_AUTH_TOKEN,
            CASE_ID
        )).thenReturn(expectedDetails);

        CaseDetails caseDetails = caseService.getCaseData(TEST_SERVICE_AUTH_TOKEN, CASE_ID);

        assertEquals(expectedDetails, caseDetails);
    }

    @Test
    void shouldGetCaseDetailsByUserReturnsData() {
        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(idamClient.getUserDetails(TEST_SERVICE_AUTH_TOKEN)).thenReturn(UserDetails.builder().id(USER_ID).build());
        when(ccdApiClient.searchForCitizen(
            TEST_SERVICE_AUTH_TOKEN,
            TEST_SERVICE_AUTH_TOKEN,
            USER_ID,
            JURISDICTION_ID,
            CASE_ID,
            emptyMap()
        )).thenReturn(expectedDetailsList);

        List<CaseDetails> caseDetails = caseService.getCaseDataByUser(
            TEST_SERVICE_AUTH_TOKEN,
            JURISDICTION_ID,
            CASE_ID,
            emptyMap()
        );

        assertEquals(expectedDetailsList, caseDetails);
    }

    @Test
    void shouldCreateNewDraftCaseInCcd() {
        CaseDataContent caseDataContent = CaseDataContent.builder()
            .event(Event.builder().id(EVENT_ID).build())
            .eventToken(startEventResponse.getToken())
            .build();
        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(ccdApiClient.startCase(TEST_SERVICE_AUTH_TOKEN, TEST_SERVICE_AUTH_TOKEN, CASE_ID, EVENT_ID)).thenReturn(
            startEventResponse);
        when(ccdApiClient.createCase(
            TEST_SERVICE_AUTH_TOKEN,
            TEST_SERVICE_AUTH_TOKEN,
            CASE_ID,
            caseDataContent
        )).thenReturn(expectedDetails);

        CaseDetails caseDetails = caseService.createCase(TEST_SERVICE_AUTH_TOKEN, CASE_ID, EVENT_ID);

        assertEquals(expectedDetails, caseDetails);
    }
}
