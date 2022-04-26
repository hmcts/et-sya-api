package uk.gov.hmcts.reform.et.syaapi.config;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ObjectMapperConfigurationTest {

    @Test
    void createObjectMapperGeneratesNewObjectMapper() {
        assertThat(new ObjectMapperConfiguration().getObjectMapper())
            .isNotNull();
    }
}