package uk.gov.hmcts.reform.et.syaapi.consumer.ccd;

import au.com.dius.pact.consumer.dsl.DslPart;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import org.json.JSONException;
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

public class StartEventForCitizenConsumerTest extends SpringBootContractBaseTest {

    private static final String ALPHABETIC_REGEX = "[/^[A-Za-z_]+$/]+";

    @Pact(provider = "ccd_data_store_cases_start_event", consumer = "et_sya_api_service")
        public RequestResponsePact startEventForCitizen(PactDslWithProvider builder) {

        Map<String, String> responseHeaders = Map.of("Content-Type", "application/json");

        return  builder
                .given("A Start event for a Citizen is requested", setUpStateMapForProviderWithoutCaseData())
                .uponReceiving("A Start event for a Citizen")
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
        @PactTestFor(pactMethod = "startEventForCitizen")
        public void verifyStartEventForCitizen() throws JSONException {

            StartEventResponse startEventResponse = coreCaseDataApi.startEventForCitizen(
                SERVICE_AUTH_TOKEN, AUTH_TOKEN, USER_ID, "EMPLOYMENT",
                "ET_EnglandWales", String.valueOf(CASE_ID), UPDATE_CASE_DRAFT);

            assertThat(startEventResponse.getEventId(), equalTo(UPDATE_CASE_DRAFT));
            assertNotNull(startEventResponse.getCaseDetails());
        }

        @Override
        protected Map<String, Object> setUpStateMapForProviderWithoutCaseData() throws JSONException {
            Map<String, Object> caseDataContentMap = super.setUpStateMapForProviderWithoutCaseData();
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
                    cd.numberType("id", null);
                    cd.stringMatcher("jurisdiction", ALPHABETIC_REGEX, "EMPLOYMENT");
                    cd.stringType("callback_response_status", null);
                    cd.stringMatcher("case_type_id", ALPHABETIC_REGEX, "ET_EnglandWales");
                    cd.object("case_data", data -> {
                    });
                });
        }).build();
    }

}
