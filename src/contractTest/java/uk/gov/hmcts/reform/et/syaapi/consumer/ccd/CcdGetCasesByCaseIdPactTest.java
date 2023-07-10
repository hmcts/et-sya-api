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

class CcdGetCasesByCaseIdPactTest extends SpringBootContractBaseTest {

    @Pact(provider = "ccdDataStoreAPI_Cases", consumer = "et_sya_api")
    RequestResponsePact executeCcdGetCasesByCaseId(PactDslWithProvider builder) {

        return builder
            .given("a case exists")
            .uponReceiving("Provider receives a GET /cases/{caseId} request from et-sya-api API")
            .path("/cases/1668618837188374/")
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
        CaseDetails caseDetails = coreCaseDataApi.getCase(SERVICE_AUTH_TOKEN, AUTH_TOKEN,
                                                          String.valueOf("1668618837188374"));

        assertThat(caseDetails.getSecurityClassification().name(), is(PUBLIC));
        assertThat(caseDetails.getJurisdiction(), is(JURISDICTION_ID));
    }

    private PactDslJsonBody createCasesResponse() {
        return new PactDslJsonBody()
            .stringType("id", String.valueOf("1668618837188374"))
            .stringValue("jurisdiction", JURISDICTION_ID)
            .stringValue("case_type", CASE_TYPE_ID)
            .stringValue("security_classification", PUBLIC);
    }
}

