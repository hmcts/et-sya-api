package uk.gov.hmcts.reform.et.syaapi.consumer.ccd;

import au.com.dius.pact.consumer.dsl.PactDslJsonBody;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.et.syaapi.consumer.SpringBootContractBaseTest;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpStatus.OK;

@PactTestFor(providerName = "ccdDataStoreAPI_Cases", port = "8891")
class CcdGetCasesByCaseIdPactTest extends SpringBootContractBaseTest {
    private static final String CCD_CASE_URL = "/cases/" + CASE_ID;
    public static final String JURISDICTION_ID = "jurisdictionId";

    @Pact(consumer = "et_sya_api")
    RequestResponsePact executeCcdGetCasesByCaseId(PactDslWithProvider builder) {

        return builder
            .given("A Get Case is requested", getStateMapForProviderWithCaseData(caseDataContent))
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
