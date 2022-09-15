package uk.gov.hmcts.reform.et.syaapi.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.et.syaapi.SyaApiApplication;
import uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants;
import uk.gov.hmcts.reform.et.syaapi.service.CaseService;
import uk.gov.hmcts.reform.et.syaapi.service.VerifyTokenService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest({AcasController.class})
@ContextConfiguration(classes = SyaApiApplication.class)
@SuppressWarnings({"PMD.LinguisticNaming"})
class AcasControllerTest {
    private final List<String> caseIds = new ArrayList<>();
    private final String requestDateTimeString = "2022-09-01T12:34:00";
    private static final String AUTH_TOKEN = "some-token";
    private static final String GET_LAST_MODIFIED_CASE_LIST_URL = "/getLastModifiedCaseList";
    private static final String GET_CASE_DATA_URL = "/getCaseData";

    @Autowired
    private WebApplicationContext webApplicationContext;

    @MockBean
    private VerifyTokenService verifyTokenService;

    @MockBean
    private CaseService caseService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        when(caseService.getCaseData(anyString(), any())).thenReturn(new ArrayList<>());
    }

    @Test
    void getLastModifiedCaseListWhenSuccessNoCasesThenReturnMsg() throws Exception {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mockMvc.perform(get(GET_LAST_MODIFIED_CASE_LIST_URL)
                            .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                            .param("datetime", requestDateTimeString))
            .andExpect(status().isOk());
    }

    @Test
    void getLastModifiedCaseListWhenSuccessCasesFoundReturnList() throws Exception {
        when(caseService.getLastModifiedCasesId(
            AUTH_TOKEN, LocalDateTime.parse("2022-09-01T12:34:00", DateTimeFormatter.ISO_LOCAL_DATE_TIME)))
            .thenReturn(List.of(1646225213651598L, 1646225213651533L, 1646225213651512L));
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mockMvc.perform(get(GET_LAST_MODIFIED_CASE_LIST_URL)
                            .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                            .param("datetime", requestDateTimeString))
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
                            .param("datetime", requestDateTimeString))
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
        when(caseService.getCaseData(AUTH_TOKEN, caseIds)).thenReturn(List.of(caseDetails));
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
}
