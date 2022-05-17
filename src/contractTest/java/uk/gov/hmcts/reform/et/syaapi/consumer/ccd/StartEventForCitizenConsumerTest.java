package uk.gov.hmcts.reform.et.syaapi.consumer.ccd;

import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.et.syaapi.consumer.SpringBootContractBaseTest;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.http.HttpMethod.GET;


class StartEventForCitizenConsumerTest extends SpringBootContractBaseTest {

    @Pact(provider = "ccd_data_store_api_cases", consumer = "et_sya_api_service")
    RequestResponsePact startEventForCitizen(PactDslWithProvider builder) {

        return  builder
                .given("A Start event for a Citizen is requested", addCaseTypeJurdisticaton())
                .uponReceiving("A Start event for a Citizen against CCD API")
                .path(buildPath())
                .method(GET.toString())
                .headers(RESPONSE_HEADERS)
                .matchHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .willRespondWith()
                .matchHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .status(200)
                .body(buildStartEventResponseWithEmptyCaseDetails(UPDATE_CASE_DRAFT))
                .toPact();
    }

    @Test
    @SneakyThrows
    @PactTestFor(pactMethod = "startEventForCitizen")
    void verifyStartEventForCitizen() {

        StartEventResponse startEventResponse = coreCaseDataApi.startEventForCitizen(SERVICE_AUTH_TOKEN, AUTH_TOKEN,
              USER_ID, JURISDICTION_ID, CASE_TYPE_ID, CASE_ID.toString(), UPDATE_CASE_DRAFT);

        assertThat(startEventResponse.getEventId(), equalTo(UPDATE_CASE_DRAFT));
        assertNotNull(startEventResponse.getCaseDetails());
    }

    private String buildPath() {
        return new StringBuilder()
            .append("/citizens/")
            .append(USER_ID)
            .append("/jurisdictions/")
            .append(JURISDICTION_ID)
            .append("/case-types/")
            .append(CASE_TYPE_ID)
            .append("/cases/")
            .append(CASE_ID)
            .append("/event-triggers/")
            .append(UPDATE_CASE_DRAFT)
            .append("/token")
            .toString();
    }
}
