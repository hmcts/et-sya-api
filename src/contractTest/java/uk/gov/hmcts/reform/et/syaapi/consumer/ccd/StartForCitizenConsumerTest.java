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
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.et.syaapi.consumer.SpringBootContractBaseTest;


import java.io.IOException;
import java.util.Map;

import static io.pactfoundation.consumer.dsl.LambdaDsl.newJsonBody;



import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class StartForCitizenConsumerTest extends SpringBootContractBaseTest {

    @Pact(provider = "ccd_data_store_api_cases", consumer = "et_sya_api_service")
        public RequestResponsePact startForCitizen(PactDslWithProvider builder) {

        Map<String, String> responseHeaders = Map.of("Content-Type", "application/json");

        return  builder
                .given("A Start for a Citizen is requested", setUpStateMapForProviderWithoutCaseData())
                .uponReceiving("A Start for a Citizen")
                .path(buildPath())
                .method("GET")
                .headers(responseHeaders)
                .matchHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .willRespondWith()
                .matchHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .status(200)
                .body(buildStartEventResponseWithEmptyCaseDetails(INITIATE_CASE_DRAFT))
                .toPact();
        }

        @Test
        @PactTestFor(pactMethod = "startForCitizen")
        public void verifyStartForCitizen() throws JSONException {

            StartEventResponse startEventResponse = coreCaseDataApi.startForCitizen(
                SERVICE_AUTH_TOKEN, AUTH_TOKEN, USER_ID, "EMPLOYMENT",
                "ET_EnglandWales", INITIATE_CASE_DRAFT);

            assertThat(startEventResponse.getEventId(), equalTo(INITIATE_CASE_DRAFT));
            assertNotNull(startEventResponse.getCaseDetails());
        }

        @Override
        protected Map<String, Object> setUpStateMapForProviderWithoutCaseData() throws JSONException {
            Map<String, Object> caseDataContentMap = super.setUpStateMapForProviderWithoutCaseData();
            caseDataContentMap.put("EVENT_ID", INITIATE_CASE_DRAFT);
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
                .append("/event-triggers/")
                .append(INITIATE_CASE_DRAFT)
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
