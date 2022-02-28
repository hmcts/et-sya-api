package uk.gov.hmcts.reform.et.syaapi.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;
import uk.gov.hmcts.reform.et.syaapi.service.PostcodeToOfficeService;

import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PostcodeController.class)
class PostcodeControllerTest {

    @MockBean
    private PostcodeToOfficeService postcodeToOfficeService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldGetTribunalOfficeWhenServiceReturnsOffice() throws Exception {

        Optional<TribunalOffice> office = Optional.of(TribunalOffice.valueOfOfficeName("Wales"));
        String postcode = "CF10 2NX";
        String expected = "\"WALES\"";

        when(postcodeToOfficeService.getTribunalOfficeFromPostcode(postcode)).thenReturn(office);
        MvcResult result = mockMvc
            .perform(get("/api/v1/postcode/" + postcode)
                         .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON))
            .andExpect(status().is(200))
            .andReturn();
        String content = result.getResponse().getContentAsString();

        assertEquals("correct office should be returned", content,expected);

    }

}
