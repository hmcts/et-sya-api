package uk.gov.hmcts.reform.et.syaapi.consumer.ccd;

import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import uk.gov.hmcts.et.common.model.ccd.Et1CaseData;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.et.syaapi.consumer.SpringBootContractBaseTest;
import uk.gov.hmcts.reform.et.syaapi.service.utils.data.ResourceLoader;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.http.HttpMethod.POST;

class SubmitForCitizenConsumerTest extends SpringBootContractBaseTest {

    @Pact(provider = "ccd_data_store_api_cases", consumer = "et_sya_api_service")
    RequestResponsePact submitForCitizen(PactDslWithProvider builder) {

        return  builder
                .given("A Submit case for a Citizen is requested", addCaseTypeJurdisticaton())
                .uponReceiving("A Submit case for a Citizen by calling CCD API")
                .path(buildPath())
                .query("ignore-warning=true")
                .method(POST.toString())
                .headers(RESPONSE_HEADERS)
                .matchHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .willRespondWith()
                .matchHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .status(201)
                .body(buildCaseDetailsDsl(CASE_ID))
                .toPact();
    }

    @Test
    @SneakyThrows
    @PactTestFor(pactMethod = "submitForCitizen")
    void verifySubmitForCitizen() {
        Et1CaseData caseData = ResourceLoader.fromString("requests/caseData_old.json", Et1CaseData.class);

        CaseDataContent caseDataContent = CaseDataContent.builder()
            .event(Event.builder().id(INITIATE_CASE_DRAFT).build())
            .eventToken(AUTH_TOKEN)
            .data(caseData)
            .build();

        CaseDetails caseDetails = coreCaseDataApi.submitForCitizen(
            SERVICE_AUTH_TOKEN, AUTH_TOKEN, USER_ID, JURISDICTION_ID,
            CASE_TYPE_ID,true, caseDataContent);

        assertNotNull(caseDetails);
    }

    private String buildPath() {
        return new StringBuilder()
            .append("/citizens/")
            .append(USER_ID)
            .append("/jurisdictions/")
            .append(JURISDICTION_ID)
            .append("/case-types/")
            .append(CASE_TYPE_ID)
            .append("/cases")
            .toString();
    }

}
