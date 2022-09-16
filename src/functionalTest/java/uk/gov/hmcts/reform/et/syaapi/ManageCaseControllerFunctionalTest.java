package uk.gov.hmcts.reform.et.syaapi;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.http.Header;
import io.restassured.path.json.JsonPath;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.ResponseCreator;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.et.syaapi.models.CaseRequest;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

@Slf4j
@TestMethodOrder(MethodOrderer.MethodName.class)
class ManageCaseControllerFunctionalTest extends BaseFunctionalTest {

    private Long caseId;
    private static final String CASE_TYPE = "ET_EnglandWales";
    private static final String CLAIMANT_EMAIL = "citizen-user-test@test.co.uk";
    private static final String AUTHORIZATION = "Authorization";
    private static final String ACAS_DEV_API_URL = "https://api-dev-acas-01.azure-api.net/ECCLDev";
    private static final String TWO_CERTS_JSON =
        "[{\"CertificateNumber\":\"AB123456/12/12\",\"CertificateDocument\":\"JVBERi0xLjcNCiW1tbW1...\"},"
            + "{\"CertificateNumber\":\"A123456/12/12\",\"CertificateDocument\":\"JVBERi0xLjcNCiW1tbW...\"}]";

    @Test
    void stage1CreateCaseShouldReturnCaseData() {
        Map<String, Object> caseData = new ConcurrentHashMap<>();
        caseData.put("caseType", "Single");
        caseData.put("caseSource", "Manually Created");
        CaseRequest caseRequest = CaseRequest.builder()
            .caseData(caseData)
            .build();

        JsonPath body = RestAssured.given()
            .contentType(ContentType.JSON)
            .header(new Header(AUTHORIZATION, userToken))
            .body(caseRequest)
            .post("/cases/initiate-case")
            .then()
            .statusCode(HttpStatus.SC_OK)
            .log().all(true)
            .extract().body().jsonPath();

        assertEquals(CASE_TYPE, body.get("case_type_id"));
        caseId = body.get("id");

        RestAssured.given()
            .contentType(ContentType.JSON)
            .header(new Header(AUTHORIZATION, userToken))
            .body(caseRequest)
            .post("/cases/initiate-case")
            .then()
            .statusCode(HttpStatus.SC_OK)
            .log().all(true);
    }

    @Test
    void stage2GetSingleCaseDetailsShouldReturnSingleCaseDetails() {
        RestAssured.given()
            .contentType(ContentType.JSON)
            .header(new Header(AUTHORIZATION, userToken))
            .body("{\"case_id\":\"" + caseId + "\"}")
            .post("/cases/user-case")
            .then()
            .statusCode(HttpStatus.SC_OK)
            .log().all(true)
            .assertThat().body("id", equalTo(caseId))
            .assertThat().body("case_type_id", equalTo(CASE_TYPE));
    }

    @Test
    void stage3GetAllCaseDetailsShouldReturnAllCaseDetails() {
        RestAssured.given()
            .contentType(ContentType.JSON)
            .header(new Header(AUTHORIZATION, userToken))
            .body("{\"case_id\":\"" + caseId + "\"}")
            .get("/cases/user-cases")
            .then()
            .statusCode(HttpStatus.SC_OK)
            .log().all(true)
            .assertThat().body("size()", is(2));
    }

    @Test
    void stage4UpdateCaseShouldReturnUpdatedCaseDetails() {
        Map<String, Object> caseData = new ConcurrentHashMap<>();
        caseData.put("claimantType", Map.of("claimant_email_address", CLAIMANT_EMAIL));

        CaseRequest caseRequest = CaseRequest.builder()
            .caseId(caseId.toString())
            .caseTypeId(CASE_TYPE)
            .caseData(caseData)
            .build();

        RestAssured.given()
            .contentType(ContentType.JSON)
            .header(new Header(AUTHORIZATION, userToken))
            .body(caseRequest)
            .put("/cases/update-case")
            .then()
            .statusCode(HttpStatus.SC_OK)
            .log().all(true)
            .assertThat().body("id", equalTo(caseId))
            .assertThat().body("case_data.claimantType.claimant_email_address", equalTo(CLAIMANT_EMAIL));
    }

    private MockRestServiceServer getMockServer() {
        return MockRestServiceServer.createServer(new RestTemplate());
    }

    private static class DelegateResponseCreator implements ResponseCreator {
        private final ResponseCreator[] delegates;
        private int toExecute;

        public DelegateResponseCreator(ResponseCreator... delegates) {
            this.delegates = Arrays.copyOf(delegates, delegates.length);
        }

        @Override
        public ClientHttpResponse createResponse(ClientHttpRequest request)
            throws IOException {
            return this.delegates[toExecute++ % delegates.length]
                .createResponse(request);
        }
    }

    @Test
    void stage5SubmitCaseShouldReturnSubmittedCaseDetails() {
        CaseRequest caseRequest = CaseRequest.builder()
            .caseId(caseId.toString())
            .caseTypeId(CASE_TYPE)
            .caseData(new ConcurrentHashMap<>())
            .build();

        getMockServer().expect(ExpectedCount.times(2), requestTo(ACAS_DEV_API_URL))
            .andExpect(method(HttpMethod.POST))
            .andRespond(new DelegateResponseCreator(withStatus(org.springframework.http.HttpStatus.UNAUTHORIZED),
                                                    withStatus(org.springframework.http.HttpStatus.OK)
                                                        .contentType(MediaType.APPLICATION_JSON)
                                                        .body(TWO_CERTS_JSON)));
        RestAssured.given()
            .contentType(ContentType.JSON)
            .header(new Header(AUTHORIZATION, userToken))
            .body(caseRequest)
            .put("/cases/submit-case")
            .then()
            .statusCode(HttpStatus.SC_OK)
            .log().all(true)
            .assertThat().body("id", equalTo(caseId))
            .assertThat().body("state", equalTo("Submitted"));
    }
}
