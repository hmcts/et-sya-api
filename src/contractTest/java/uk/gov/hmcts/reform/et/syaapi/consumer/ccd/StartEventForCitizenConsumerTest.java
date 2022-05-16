package uk.gov.hmcts.reform.et.syaapi.consumer.ccd;

import au.com.dius.pact.consumer.dsl.DslPart;
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

import java.util.Map;

import static io.pactfoundation.consumer.dsl.LambdaDsl.newJsonBody;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class StartEventForCitizenConsumerTest extends SpringBootContractBaseTest {

    @Pact(provider = "ccd_data_store_api_cases", consumer = "et_sya_api_service")
    public RequestResponsePact startEventForCitizen(PactDslWithProvider builder) {

        Map<String, String> responseHeaders = Map.of("Content-Type", "application/json");

        return  builder
                .given("A Start event for a Citizen is requested", addCaseTypeJurdisticaton())
                .uponReceiving("A Start event for a Citizen against CCD API")
                .path(buildPath())
                .method("GET")
                .headers(responseHeaders)
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

        StartEventResponse startEventResponse = coreCaseDataApi.startEventForCitizen(
            SERVICE_AUTH_TOKEN, AUTH_TOKEN, USER_ID, "EMPLOYMENT",
            "ET_EnglandWales", CASE_ID.toString(), UPDATE_CASE_DRAFT);

        assertThat(startEventResponse.getEventId(), equalTo(UPDATE_CASE_DRAFT));
        assertNotNull(startEventResponse.getCaseDetails());
    }

    @Override
    @SneakyThrows
    protected Map<String, Object> addCaseTypeJurdisticaton() {
        Map<String, Object> caseDataContentMap = super.addCaseTypeJurdisticaton();
        caseDataContentMap.put("EVENT_ID", UPDATE_CASE_DRAFT);
        return caseDataContentMap;
    }

    private String buildPath() {
        return new StringBuilder()
            .append("/citizens/")
            .append(USER_ID)
            .append("/jurisdictions/")
            .append("EMPLOYMENT")
            .append("/case-types/")
            .append("ET_EnglandWales")
            .append("/cases/")
            .append(CASE_ID)
            .append("/event-triggers/")
            .append(UPDATE_CASE_DRAFT)
            .append("/token")
            .toString();
    }

    public static DslPart buildStartEventResponseWithEmptyCaseDetails(String eventId) {
        return newJsonBody((o) -> {
            o.stringType("event_id", eventId)
                .stringType("token", null)
                .object("case_details", (cd) -> {
                    cd.numberType("id", CASE_ID);
                    cd.stringMatcher("jurisdiction", ALPHABETIC_REGEX, "EMPLOYMENT");
                    cd.stringType("callback_response_status", null);
                    cd.stringMatcher("case_type_id", ALPHABETIC_REGEX, "ET_EnglandWales");
                    cd.object("case_data", data -> {
                    });
                });
        }).build();
    }

}
