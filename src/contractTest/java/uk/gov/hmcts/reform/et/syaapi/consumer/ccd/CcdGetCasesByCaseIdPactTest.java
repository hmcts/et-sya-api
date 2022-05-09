package uk.gov.hmcts.reform.et.syaapi.consumer.ccd;

import au.com.dius.pact.consumer.dsl.PactDslJsonBody;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.et.syaapi.client.CcdApiClient;
import uk.gov.hmcts.reform.et.syaapi.service.CaseService;
import uk.gov.hmcts.reform.idam.client.IdamClient;

import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.et.syaapi.consumer.SpringBootContractBaseTest.SERVICE_AUTH_TOKEN;

public class CcdGetCasesByCaseIdPactTest {

    private static final String TEST_CASE_ID = "1607103938250138";
    private static final String CCD_CASE_URL = "/cases/" + TEST_CASE_ID;

    @Autowired
    CcdApiClient ccdApiClient;

    @MockBean
    AuthTokenGenerator authTokenGenerator;

    @MockBean
    IdamClient idamClient;

    private CaseService caseService;

    @BeforeEach
    void setUp() {
        when(authTokenGenerator.generate()).thenReturn(SERVICE_AUTH_TOKEN);
        //ccdDataService = new CcdDataService(ccdApiClient, authTokenGenerator, systemTokenGenerator);
        caseService = new CaseService();
    }

    @Pact(provider = "ccd_data_store_get_case_by_id", consumer = "et_sya_api")
    public RequestResponsePact executeCcdGetCasesByCaseId(PactDslWithProvider builder) {

        Map<String, String> responseHeaders = Map.of("Content-Type", "application/json");

        return builder
            .given("a case exists")
            .uponReceiving("Provider receives a GET /cases/{caseId} request from et-sya-api API")
            .path(CCD_CASE_URL)
            .method(HttpMethod.GET.toString())
            .willRespondWith()
            .status(HttpStatus.OK.value())
            .headers(responseHeaders)
            .body(createCasesResponse())
            .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "executeCcdGetCasesByCaseId")
    public void verifyGetCaseById() {
        CaseDetails caseDetails = caseService.getCaseData(SERVICE_AUTH_TOKEN, TEST_CASE_ID);

        assertThat(caseDetails.getSecurityClassification(), is("PRIVATE"));
        assertThat(caseDetails.getJurisdiction(), is("EMPLOYMENT"));
    }

    private PactDslJsonBody createCasesResponse() {
        return new PactDslJsonBody()
            .stringType("id", "1593694526480034")
            .stringValue("jurisdiction", "EMPLOYMENT")
            .stringValue("case_type", "ET_EnglandWaltes")
            .stringValue("security_classification", "PRIVATE");
    }

}

