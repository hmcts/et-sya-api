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
import uk.gov.hmcts.reform.et.syaapi.models.EmploymentCaseData;
import uk.gov.hmcts.reform.et.syaapi.utils.ResourceLoader;
import uk.gov.hmcts.reform.et.syaapi.utils.ResourceUtil;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.et.syaapi.utils.TestConstants.TEST_SERVICE_AUTH_TOKEN;

@ExtendWith(MockitoExtension.class)
class CaseServiceTest {
    private static final String CASE_ID = "ET_Scotland";
    private static final String EVENT_ID = "initiateCaseDraft";
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
    private final EmploymentCaseData caseData = ResourceLoader.fromString(
        "requests/caseData.json",
        EmploymentCaseData.class
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
            CASE_ID
        )).thenReturn(expectedDetails);

        CaseDetails caseDetails = caseService.getCaseData(TEST_SERVICE_AUTH_TOKEN, CASE_ID);

        assertEquals(expectedDetails, caseDetails);
    }

    @Test
    void shouldCreateNewDraftCaseInCcd() {
        CaseDataContent caseDataContent = CaseDataContent.builder()
            .event(Event.builder().id(EVENT_ID).build())
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
            JURISDICTION_ID,
            CASE_ID,
            EVENT_ID
        )).thenReturn(
            startEventResponse);
        when(ccdApiClient.submitForCaseworker(
            TEST_SERVICE_AUTH_TOKEN,
            TEST_SERVICE_AUTH_TOKEN,
            "12",
            JURISDICTION_ID,
            CASE_ID,
            true,
            caseDataContent
        )).thenReturn(expectedDetails);

        CaseDetails caseDetails = caseService.createCase(TEST_SERVICE_AUTH_TOKEN, CASE_ID, EVENT_ID, requestCaseData);

        assertEquals(expectedDetails, caseDetails);
    }
}
