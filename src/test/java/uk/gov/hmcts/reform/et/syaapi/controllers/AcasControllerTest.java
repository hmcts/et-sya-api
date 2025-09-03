package uk.gov.hmcts.reform.et.syaapi.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.et.syaapi.SyaApiApplication;
import uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants;
import uk.gov.hmcts.reform.et.syaapi.models.CaseDocumentAcasResponse;
import uk.gov.hmcts.reform.et.syaapi.service.AcasCaseService;
import uk.gov.hmcts.reform.et.syaapi.service.AdminUserService;
import uk.gov.hmcts.reform.et.syaapi.service.CaseDocumentService;
import uk.gov.hmcts.reform.et.syaapi.service.FeatureToggleService;
import uk.gov.hmcts.reform.et.syaapi.service.VerifyTokenService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest({AcasController.class})
@ContextConfiguration(classes = SyaApiApplication.class)
@SuppressWarnings({"PMD.LinguisticNaming", "PMD.TooManyMethods"})
class AcasControllerTest {
    public static final String CASE_ID_PARAM = "caseId";
    private final List<String> caseIds = new ArrayList<>();
    private static final String REQUEST_DATE_TIME_STRING = "2022-09-01T12:34:00";
    private static final String AUTH_TOKEN = "some-token";
    private static final String GET_LAST_MODIFIED_CASE_LIST_URL = "/getLastModifiedCaseList";
    private static final String GET_CASE_DATA_URL = "/getCaseData";
    private static final String GET_ACAS_DOCUMENTS_URL = "/getAcasDocuments";
    private static final String DOWNLOAD_ACAS_DOCUMENTS_URL = "/downloadAcasDocuments";
    private static final String VET_AND_ACCEPT_CASE = "/vetAndAcceptCase";

    @Autowired
    private WebApplicationContext webApplicationContext;
    @MockBean
    private VerifyTokenService verifyTokenService;
    @MockBean
    private AcasCaseService acasCaseService;
    @MockBean
    private FeatureToggleService featureToggleService;
    @MockBean
    private CaseDocumentService caseDocumentService;

    @MockBean
    private AdminUserService adminUserService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        when(acasCaseService.getCaseData(anyString(), any())).thenReturn(new ArrayList<>());
    }

    @Test
    void getLastModifiedCaseListWhenSuccessNoCasesThenReturnMsg() throws Exception {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mockMvc.perform(get(GET_LAST_MODIFIED_CASE_LIST_URL)
                            .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                            .param("datetime", REQUEST_DATE_TIME_STRING))
            .andExpect(status().isOk());
    }

    @Test
    void getLastModifiedCaseListWhenSuccessCasesFoundReturnList() throws Exception {
        when(acasCaseService.getLastModifiedCasesId(
            AUTH_TOKEN, LocalDateTime.parse("2022-09-01T12:34:00", DateTimeFormatter.ISO_LOCAL_DATE_TIME)))
            .thenReturn(List.of(1_646_225_213_651_598L, 1_646_225_213_651_533L, 1_646_225_213_651_512L));
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mockMvc.perform(get(GET_LAST_MODIFIED_CASE_LIST_URL)
                            .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                            .param("datetime", REQUEST_DATE_TIME_STRING))
            .andExpect(status().isOk());
    }

    @Test
    void getLastModifiedCaseListWhenNoParameterReturnError() throws Exception {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mockMvc.perform(get(GET_LAST_MODIFIED_CASE_LIST_URL)
                            .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN))
            .andExpect(status().isBadRequest());
    }

    @Test
    void getLastModifiedCaseListWhenInvalidTokenReturnError() throws Exception {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mockMvc.perform(get(GET_LAST_MODIFIED_CASE_LIST_URL)
                            .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                            .param("datetime", REQUEST_DATE_TIME_STRING))
            .andExpect(status().isForbidden());
    }

    @Test
    void getCaseDataSuccessNoCases() throws Exception {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mockMvc.perform(get(GET_CASE_DATA_URL)
                            .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                            .param("caseIds", caseIds.toString()))
            .andExpect(status().isOk());
    }

    @Test
    void getCaseDataSuccessCasesFound() throws Exception {
        CaseDetails caseDetails = CaseDetails.builder()
            .caseTypeId(EtSyaConstants.SCOTLAND_CASE_TYPE)
            .id(123_456_789L)
            .build();
        when(acasCaseService.getCaseData(AUTH_TOKEN, caseIds)).thenReturn(List.of(caseDetails));
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mockMvc.perform(get(GET_CASE_DATA_URL)
                            .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                            .param("caseIds", caseIds.toString()))
            .andExpect(status().isOk());
    }

    @Test
    void getCaseDataNoParameter() throws Exception {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mockMvc.perform(get(GET_CASE_DATA_URL)
                            .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN))
            .andExpect(status().isBadRequest());
    }

    @Test
    void getCaseDataInvalidToken() throws Exception {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mockMvc.perform(get(GET_CASE_DATA_URL)
                            .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                            .param("caseIds", caseIds.toString()))
            .andExpect(status().isForbidden());
    }

    @Test
    void getAcasDocumentsDocumentsFound() throws Exception {
        List<CaseDocumentAcasResponse> acasDocs = new ArrayList<>();
        CaseDocumentAcasResponse caseDocumentAcasResponse = CaseDocumentAcasResponse.builder()
            .documentType("ET1")
            .documentId(UUID.randomUUID().toString())
            .modifiedOn("2023-02-06T12:41:47.000+00:00")
            .build();
        acasDocs.add(caseDocumentAcasResponse);
        when(acasCaseService.retrieveAcasDocuments(anyString())).thenReturn(acasDocs);
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);

        mockMvc.perform(get(GET_ACAS_DOCUMENTS_URL)
                            .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                            .param(CASE_ID_PARAM, "123"))
            .andExpect(status().isOk());
    }

    @Test
    void getAcasDocumentsNoParameter() throws Exception {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mockMvc.perform(get(GET_ACAS_DOCUMENTS_URL)
                            .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN))
            .andExpect(status().isBadRequest());
    }

    @Test
    void getAcasDocumentsInvalidToken() throws Exception {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mockMvc.perform(get(GET_ACAS_DOCUMENTS_URL)
                            .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                            .param(CASE_ID_PARAM, "dummy"))
            .andExpect(status().isForbidden());
    }

    @Test
    void downloadAcasDocumentsDocumentsFound() throws Exception {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        when(adminUserService.getAdminUserToken()).thenReturn(AUTH_TOKEN);
        when(caseDocumentService.downloadDocument(anyString(), any())).thenReturn(getDocumentBinaryContent());
        mockMvc.perform(get(DOWNLOAD_ACAS_DOCUMENTS_URL)
                            .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                            .param("documentId", UUID.randomUUID().toString()))
            .andExpect(status().isOk());
    }

    @Test
    void downloadAcasDocumentsNoParameter() throws Exception {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mockMvc.perform(get(DOWNLOAD_ACAS_DOCUMENTS_URL)
                            .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN))
            .andExpect(status().isBadRequest());
    }

    @Test
    void downloadAcasDocumentsInvalidToken() throws Exception {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mockMvc.perform(get(DOWNLOAD_ACAS_DOCUMENTS_URL)
                            .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                            .param("documentId", UUID.randomUUID().toString()))
            .andExpect(status().isForbidden());
    }

    @Test
    void vetAndAcceptCaseFeatureToggleDisabled() throws Exception {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        when(featureToggleService.isAcasVetAndAcceptEnabled()).thenReturn(false);
        mockMvc.perform(post(VET_AND_ACCEPT_CASE)
                            .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                            .param(CASE_ID_PARAM, "123"))
            .andExpect(status().isForbidden());
    }

    @Test
    void vetAndAcceptCaseInvalidCaseId() throws Exception {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        when(featureToggleService.isAcasVetAndAcceptEnabled()).thenReturn(true);
        mockMvc.perform(post(VET_AND_ACCEPT_CASE)
                            .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                            .param(CASE_ID_PARAM, "1234"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void vetAndAcceptCaseValidCaseId() throws Exception {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        when(featureToggleService.isAcasVetAndAcceptEnabled()).thenReturn(true);
        when(acasCaseService.vetAndAcceptCase("1234567890123456"))
            .thenReturn(CaseDetails.builder()
                            .id(1_234_567_890_123_456L)
                            .caseTypeId(EtSyaConstants.SCOTLAND_CASE_TYPE)
                            .build());
        mockMvc.perform(post(VET_AND_ACCEPT_CASE)
                            .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                            .param(CASE_ID_PARAM, "1234567890123456"))
            .andExpect(status().isOk());
    }

    private ResponseEntity<ByteArrayResource> getDocumentBinaryContent() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json; charset=utf-8");
        return new ResponseEntity<>(
            new ByteArrayResource("test document content".getBytes()),
            headers,
            HttpStatus.OK
        );
    }
}
