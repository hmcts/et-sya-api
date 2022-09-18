package uk.gov.hmcts.reform.et.syaapi.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants;
import uk.gov.hmcts.reform.et.syaapi.model.TestData;
import uk.gov.hmcts.reform.et.syaapi.service.pdf.PdfDecodedMultipartFile;
import uk.gov.hmcts.reform.et.syaapi.service.pdf.PdfServiceException;
import uk.gov.hmcts.reform.idam.client.IdamApi;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.et.syaapi.utils.TestConstants.CITIZEN_PORTAL_LINK;
import static uk.gov.hmcts.reform.et.syaapi.utils.TestConstants.SUBMIT_CASE_DRAFT;
import static uk.gov.hmcts.reform.et.syaapi.utils.TestConstants.SUBMIT_CASE_EMAIL_TEMPLATE_ID;
import static uk.gov.hmcts.reform.et.syaapi.utils.TestConstants.TEST_SERVICE_AUTH_TOKEN;
import static uk.gov.hmcts.reform.et.syaapi.utils.TestConstants.USER_ID;

@RunWith(SpringRunner.class)
@SpringBootTest
class CaseServiceBootTest {
    @Autowired
    CaseService caseService;
    @MockBean
    private AuthTokenGenerator authTokenGenerator;
    @MockBean
    private IdamClient idamClient;
    @MockBean
    private IdamApi idamApi;
    @MockBean
    private CoreCaseDataApi ccdApiClient;
    @MockBean
    private CaseDocumentService caseDocumentService;
    @MockBean
    NotificationService notificationService;
    private TestData testData;

    @BeforeEach
    void beforeEach() {
        testData = new TestData();
    }

    @Test
    void theSubmitCaseProducesCaseDetails()
        throws CaseDocumentException, AcasException, PdfServiceException, InvalidAcasNumbersException {
        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(idamApi.retrieveUserDetails(TEST_SERVICE_AUTH_TOKEN)).thenReturn(new UserDetails(
            USER_ID,
            testData.getCaseData().getClaimantType().getClaimantEmailAddress(),
            testData.getCaseData().getClaimantIndType().getClaimantFirstNames(),
            testData.getCaseData().getClaimantIndType().getClaimantLastName(),
            null
        ));
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
            testData.getCaseData().getEcmCaseType(),
            testData.getCaseRequest().getCaseId(),
            SUBMIT_CASE_DRAFT
        )).thenReturn(
            testData.getStartEventResponse());
        when(ccdApiClient.submitEventForCitizen(
            eq(TEST_SERVICE_AUTH_TOKEN),
            eq(TEST_SERVICE_AUTH_TOKEN),
            eq(USER_ID),
            eq(EtSyaConstants.JURISDICTION_ID),
            eq(testData.getCaseData().getEcmCaseType()),
            eq(testData.getCaseRequest().getCaseId()),
            eq(true),
            any(CaseDataContent.class)
        )).thenReturn(testData.getExpectedDetails());
        when(caseDocumentService.uploadAllDocuments(
            eq(TEST_SERVICE_AUTH_TOKEN),
            eq(testData.getCaseData().getEcmCaseType()),
            any(PdfDecodedMultipartFile.class),
            anyList()
            )).thenReturn(testData.getUploadDocumentResponse());
        when(notificationService.sendSubmitCaseConfirmationEmail(
            eq(SUBMIT_CASE_EMAIL_TEMPLATE_ID),
            eq(testData.getCaseData().getClaimantType().getClaimantEmailAddress()),
            eq(testData.getCaseRequest().getCaseId()),
            eq(testData.getCaseData().getClaimantIndType().getClaimantTitle()),
            eq(testData.getCaseData().getClaimantIndType().getClaimantLastName()),
            any(String.class),
            eq(CITIZEN_PORTAL_LINK)
        )).thenReturn(null);
        CaseDetails caseDetails = caseService.submitCase(TEST_SERVICE_AUTH_TOKEN, testData.getCaseRequest());
        assertEquals(caseDetails.getId(), testData.getExpectedDetails().getId());
        assertEquals(caseDetails.getJurisdiction(), testData.getExpectedDetails().getJurisdiction());
        assertEquals(caseDetails.getCaseTypeId(), testData.getExpectedDetails().getCaseTypeId());
        assertEquals(caseDetails.getState(), testData.getExpectedDetails().getState());
    }
}
