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

class StartForCitizenConsumerTest extends SpringBootContractBaseTest {

    @Pact(provider = "ccd_data_store_api_cases", consumer = "et_sya_api_service")
        RequestResponsePact startForCitizen(PactDslWithProvider builder) {

        return  builder
                .given("A Start case for a Citizen is requested", addCaseTypeJurisdiction())
                .uponReceiving("A Start case for a Citizen by calling CCD API")
                .path(buildPath())
                .method(GET.toString())
                .headers(RESPONSE_HEADERS)
                .matchHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .willRespondWith()
                .matchHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .status(200)
                .body(buildStartEventResponseWithEmptyCaseDetails(INITIATE_CASE_DRAFT))
                .toPact();
    }

    @Test
    @SneakyThrows
    @PactTestFor(pactMethod = "startForCitizen")
    void verifyStartForCitizen() {

        StartEventResponse startEventResponse = coreCaseDataApi.startForCitizen(
            SERVICE_AUTH_TOKEN, AUTH_TOKEN, USER_ID, JURISDICTION_ID, CASE_TYPE_ID, INITIATE_CASE_DRAFT);

        assertThat(startEventResponse.getEventId(), equalTo(INITIATE_CASE_DRAFT));
        assertNotNull(startEventResponse.getCaseDetails());
    }

    private String buildPath() {
        return "/citizens/"
            + USER_ID
            + "/jurisdictions/"
            + JURISDICTION_ID
            + "/case-types/"
            + CASE_TYPE_ID
            + "/event-triggers/"
            + INITIATE_CASE_DRAFT
            + "/token";
    }
}
