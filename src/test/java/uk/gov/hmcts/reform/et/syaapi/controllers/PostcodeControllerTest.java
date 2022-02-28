package uk.gov.hmcts.reform.et.syaapi.controllers;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;
import uk.gov.hmcts.reform.et.syaapi.service.PostcodeToOfficeService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.util.Optional;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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

        when(postcodeToOfficeService.getTribunalOfficeFromPostcode(postcode)).thenReturn(office);
        this.mockMvc
            .perform(get("/api/v1/postcode/")
                         .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON))
            .andExpect(status().is(200))
            .andExpect(jsonPath("$.size()", is(1)));

    }

}
