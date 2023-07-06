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
import uk.gov.hmcts.reform.et.syaapi.exception.PdfServiceException;
import uk.gov.hmcts.reform.et.syaapi.model.CaseTestData;
import uk.gov.hmcts.reform.idam.client.IdamApi;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.TEST_SERVICE_AUTH_TOKEN;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.USER_ID;

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
    private NotificationService notificationService;
    @MockBean
    private AcasService acasService;
    private CaseTestData caseTestData;

    @BeforeEach
    void beforeEach() throws CaseDocumentException {
        caseTestData = new CaseTestData();

        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(idamApi.retrieveUserInfo(TEST_SERVICE_AUTH_TOKEN)).thenReturn(new UserInfo(
            null,
            USER_ID,
            "name",
            caseTestData.getCaseData().getClaimantIndType().getClaimantFirstNames(),
            caseTestData.getCaseData().getClaimantIndType().getClaimantLastName(),
            null
        ));
        when(idamClient.getUserInfo(TEST_SERVICE_AUTH_TOKEN)).thenReturn(new UserInfo(
            null,
            USER_ID,
            "name",
            caseTestData.getCaseData().getClaimantIndType().getClaimantFirstNames(),
            caseTestData.getCaseData().getClaimantIndType().getClaimantLastName(),
            null
        ));
        when(ccdApiClient.startEventForCitizen(
            eq(TEST_SERVICE_AUTH_TOKEN),
            eq(TEST_SERVICE_AUTH_TOKEN),
            eq(USER_ID),
            eq(EtSyaConstants.JURISDICTION_ID),
            eq(caseTestData.getCaseRequest().getCaseTypeId()),
            eq(caseTestData.getCaseRequest().getCaseId()),
            any()
        )).thenReturn(
            caseTestData.getStartEventResponse());
        when(ccdApiClient.submitEventForCitizen(
            eq(TEST_SERVICE_AUTH_TOKEN),
            eq(TEST_SERVICE_AUTH_TOKEN),
            eq(USER_ID),
            eq(EtSyaConstants.JURISDICTION_ID),
            eq(caseTestData.getCaseRequest().getCaseTypeId()),
            eq(caseTestData.getCaseRequest().getCaseId()),
            eq(true),
            any(CaseDataContent.class)
        )).thenReturn(caseTestData.getExpectedDetails());
        when(caseDocumentService.uploadAllDocuments(
            eq(TEST_SERVICE_AUTH_TOKEN),
            eq(caseTestData.getCaseRequest().getCaseTypeId()),
            anyList(),
            anyList()
        )).thenReturn(caseTestData.getUploadDocumentResponse());
        when(notificationService.sendSubmitCaseConfirmationEmail(
            eq(caseTestData.getCaseRequest()),
            eq(caseTestData.getCaseData()),
            eq(caseTestData.getUserInfo()),
            any())
        ).thenReturn(null);
    }

    @Test
    void theSubmitCaseProducesCaseDetails()
        throws PdfServiceException {
        when(acasService.getAcasCertificatesByCaseData(caseTestData.getCaseData())).thenReturn(
            new ArrayList<>()
        );

        CaseDetails caseDetails = caseService.submitCase(TEST_SERVICE_AUTH_TOKEN, caseTestData.getCaseRequest());
        assertEquals(caseDetails.getId(), caseTestData.getExpectedDetails().getId());
        assertEquals(caseDetails.getJurisdiction(), caseTestData.getExpectedDetails().getJurisdiction());
        assertEquals(caseDetails.getCaseTypeId(), caseTestData.getExpectedDetails().getCaseTypeId());
        assertEquals(caseDetails.getState(), caseTestData.getExpectedDetails().getState());
    }
}
