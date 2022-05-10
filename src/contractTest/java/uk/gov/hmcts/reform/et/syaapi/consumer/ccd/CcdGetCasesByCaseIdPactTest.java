package uk.gov.hmcts.reform.et.syaapi.consumer.ccd;

import au.com.dius.pact.consumer.dsl.PactDslJsonBody;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.et.syaapi.consumer.SpringBootContractBaseTest;

import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpStatus.OK;

@PactTestFor(providerName = "ccd_data_store_get_casebyid", port = "8890")
class CcdGetCasesByCaseIdPactTest extends SpringBootContractBaseTest {

    private static final String TEST_CASE_ID = "1593694526480034";
    private static final String CCD_CASE_URL = "/cases/" + TEST_CASE_ID;

    @Autowired
    protected CoreCaseDataApi coreCaseDataApi;

    @Pact(provider = "ccd_data_store_get_case_by_id", consumer = "et_sya_api_service")
    RequestResponsePact executeCcdGetCasesByCaseId(PactDslWithProvider builder) {

        Map<String, String> responseHeaders = Map.of("Content-Type", "application/json");

        return builder
            .given("a case exists")
            .uponReceiving("Provider receives a GET /cases/{caseId} request from et-sya-api API")
            .path(CCD_CASE_URL)
            .method(GET.toString())
            .willRespondWith()
            .status(OK.value())
            .headers(responseHeaders)
            .body(createCasesResponse())
            .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "executeCcdGetCasesByCaseId")
    void verifyGetCaseById() {
        CaseDetails caseDetails = coreCaseDataApi.getCase(SERVICE_AUTH_TOKEN, AUTH_TOKEN, TEST_CASE_ID);

        assertThat(caseDetails.getSecurityClassification().name(), is("PUBLIC"));
        assertThat(caseDetails.getJurisdiction(), is("EMPLOYMENT"));
    }

    private PactDslJsonBody createCasesResponse() {
        return new PactDslJsonBody()
            .stringType("id", "1593694526480034")
            .stringValue("jurisdiction", "EMPLOYMENT")
            .stringValue("case_type", "ET_EnglandWaltes")
            .stringValue("security_classification", "PUBLIC");
    }

}

