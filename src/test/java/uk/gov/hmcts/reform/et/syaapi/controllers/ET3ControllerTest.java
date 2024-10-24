package uk.gov.hmcts.reform.et.syaapi.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import uk.gov.hmcts.et.common.model.ccd.Et3Request;
import uk.gov.hmcts.reform.et.syaapi.SyaApiApplication;
import uk.gov.hmcts.reform.et.syaapi.model.CaseTestData;
import uk.gov.hmcts.reform.et.syaapi.service.ET3Service;
import uk.gov.hmcts.reform.et.syaapi.service.VerifyTokenService;
import uk.gov.hmcts.reform.et.syaapi.service.utils.ResourceLoader;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.GET_SERVICE_FIND_CASE_FOR_ROLE_MODIFICATION_URL;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.PARAMETER_NAME_ETHOS_CASE_REFERENCE;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.POST_SERVICE_MODIFY_ET3_DATA_URL;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.TEST_ETHOS_CASE_REFERENCE;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.TEST_SERVICE_AUTH_TOKEN;

@RunWith(SpringRunner.class)
@WebMvcTest({ET3Controller.class})
@ContextConfiguration(classes = SyaApiApplication.class)
class ET3ControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @MockBean
    private VerifyTokenService verifyTokenService;

    @MockBean
    private ET3Service et3Service;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    void modifyEt3Data() throws Exception {
        Et3Request et3Request = Et3Request.builder().build();
        when(verifyTokenService.verifyTokenSignature(TEST_SERVICE_AUTH_TOKEN)).thenReturn(true);
        when(et3Service.modifyEt3Data(TEST_SERVICE_AUTH_TOKEN, et3Request))
            .thenReturn(new CaseTestData().getCaseDetails());
        mockMvc.perform(post(POST_SERVICE_MODIFY_ET3_DATA_URL)
                            .header(HttpHeaders.AUTHORIZATION, TEST_SERVICE_AUTH_TOKEN)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(ResourceLoader.toJson(et3Request)))
            .andExpect(status().isOk());
    }

    @Test
    void checkCaseByEthosCaseReference() throws Exception {
        when(verifyTokenService.verifyTokenSignature(TEST_SERVICE_AUTH_TOKEN)).thenReturn(true);
        when(et3Service.findCaseByEthosCaseReference(TEST_ETHOS_CASE_REFERENCE))
            .thenReturn(new CaseTestData().getCaseDetails());
        mockMvc.perform(get(GET_SERVICE_FIND_CASE_FOR_ROLE_MODIFICATION_URL)
                            .header(HttpHeaders.AUTHORIZATION, TEST_SERVICE_AUTH_TOKEN)
                            .param(PARAMETER_NAME_ETHOS_CASE_REFERENCE, TEST_ETHOS_CASE_REFERENCE))
            .andExpect(status().isOk());
    }
}
