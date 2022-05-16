package uk.gov.hmcts.reform.et.syaapi.controllers;

import org.junit.Before;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.MockitoAnnotations.openMocks;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class RootControllerTest {
    MockMvc mockMvc;
    RootController controller;

    @Before
    public void setup() {
        openMocks(this);
        controller = new RootController();
        this.mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    public void welcome() throws Exception {
        mockMvc.perform(get("/")).andExpect(status().isOk());
    }

}
