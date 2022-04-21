package uk.gov.hmcts.reform.et.syaapi.config;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RestTemplateConfigurationTest {

    @Test
    void createRestTemplateGeneratesNewRestTemplate() {
        assertThat(new RestTemplateConfiguration().getRestTemplate())
            .isNotNull();
    }
}
