package uk.gov.hmcts.reform.et.syaapi.consumer.ccd;

import au.com.dius.pact.consumer.dsl.PactDslJsonBody;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.et.syaapi.consumer.SpringBootContractTestBase;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpStatus.OK;


class CcdGetCasesByCaseIdPactTest extends SpringBootContractTestBase {
    private static final String CCD_CASE_URL = "/cases/" + CASE_ID;

    @Pact(provider = "ccd_data_store_api_cases", consumer = "et_sya_api_service")
    RequestResponsePact executeCcdGetCasesByCaseId(PactDslWithProvider builder) {

        return builder
            .given("A Get Case is requested")
            .uponReceiving("Provider receives a GET /cases/{caseId} request from et-sya-api API")
            .path(CCD_CASE_URL)
            .method(GET.toString())
            .willRespondWith()
            .status(OK.value())
            .headers(RESPONSE_HEADERS)
            .body(createCasesResponse())
            .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "executeCcdGetCasesByCaseId")
    void verifyGetCaseById() {
        CaseDetails caseDetails = coreCaseDataApi.getCase(SERVICE_AUTH_TOKEN, AUTH_TOKEN, String.valueOf(CASE_ID));

        assertThat(caseDetails.getSecurityClassification().name(), is(PUBLIC));
        assertThat(caseDetails.getJurisdiction(), is(JURISDICTION_ID));
    }

    private PactDslJsonBody createCasesResponse() {
        return new PactDslJsonBody()
            .stringType("id", String.valueOf(CASE_ID))
            .stringValue("jurisdiction", JURISDICTION_ID)
            .stringValue("case_type", CASE_TYPE_ID)
            .stringValue("security_classification", PUBLIC);
    }
}

