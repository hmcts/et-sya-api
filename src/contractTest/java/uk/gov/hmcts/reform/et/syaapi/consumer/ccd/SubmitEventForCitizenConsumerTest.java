package uk.gov.hmcts.reform.et.syaapi.consumer.ccd;

import au.com.dius.pact.consumer.dsl.DslPart;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import uk.gov.hmcts.et.common.model.ccd.Et1CaseData;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.et.syaapi.consumer.SpringBootContractBaseTest;
import uk.gov.hmcts.reform.et.syaapi.utils.ResourceLoader;

import java.util.Map;

import static io.pactfoundation.consumer.dsl.LambdaDsl.newJsonBody;

public class SubmitEventForCitizenConsumerTest extends SpringBootContractBaseTest {

    @Pact(provider = "ccd_data_store_api_cases", consumer = "et_sya_api_service")
        public RequestResponsePact submitEventForCitizen(PactDslWithProvider builder) {

        Map<String, String> responseHeaders = Map.of("Content-Type", "application/json");

        return  builder
                .given("A Submit event for a Citizen is requested", setUpStateMapForProviderWithoutCaseData())
                .uponReceiving("A Submit event for a Citizen")
                .path(buildPath())
                .query("ignore-warning=true")
                .method(HttpMethod.POST.toString())
                .headers(responseHeaders)
                .matchHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                //.body("{}")
                .willRespondWith()
                .matchHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .status(HttpStatus.CREATED.value())
                .body(buildStartEventResponseWithEmptyCaseDetails())
                .toPact();
        }

        @Test
        @PactTestFor(pactMethod = "submitEventForCitizen")
        public void verifysubmitEventForCitizen() throws JSONException {
            Et1CaseData caseData = ResourceLoader.fromString(
                "requests/caseData.json",
                Et1CaseData.class
            );

           CaseDataContent caseDataContent = CaseDataContent.builder()
                .event(Event.builder().id(UPDATE_CASE_DRAFT).build())
                .eventToken(AUTH_TOKEN)
                .data(caseData)
                .build();

            CaseDetails caseDetails = coreCaseDataApi.submitEventForCitizen(
                SERVICE_AUTH_TOKEN, AUTH_TOKEN, USER_ID, "EMPLOYMENT",
                "ET_EnglandWales", CASE_ID.toString(), true, caseDataContent);

            System.out.println(caseDetails);
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
            .append("/events")
            .toString();
    }
    public static DslPart buildStartEventResponseWithEmptyCaseDetails() {
        return newJsonBody((o) -> {
            o.numberType("id", CASE_ID)
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
