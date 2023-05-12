package uk.gov.hmcts.reform.et.syaapi;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.http.Header;
import io.restassured.path.json.JsonPath;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import uk.gov.hmcts.reform.et.syaapi.models.CaseRequest;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;

@SuppressWarnings("PMD.LawOfDemeter")
@Slf4j
@TestMethodOrder(MethodOrderer.MethodName.class)
class ManageCaseControllerFunctionalTest extends BaseFunctionalTest {

    private Long caseId;
    private static final String CASE_TYPE = "ET_EnglandWales";
    private static final String CLAIMANT_EMAIL = "citizen-user-test@test.co.uk";
    private static final String AUTHORIZATION = "Authorization";

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

    @SneakyThrows
    @Test
    void stage3GetAllCaseDetailsShouldReturnAllCaseDetails() {
        TimeUnit.SECONDS.sleep(2);
        RestAssured.given()
            .contentType(ContentType.JSON)
            .header(new Header(AUTHORIZATION, userToken))
            .get("/cases/user-cases")
            .then()
            .statusCode(HttpStatus.SC_OK)
            .log().all(true)
            .assertThat().body("size()", is(2));
    }

    @Test
    void stage4UpdateCaseShouldReturnUpdatedDraftCaseDetails() {
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

    /*
    @Test
    void stage5SubmitCaseShouldReturnSubmittedCaseDetails() {
        Map<String, Object> caseData = new ConcurrentHashMap<>();
        caseData.put("receiptDate", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        CaseRequest caseRequest = CaseRequest.builder()
            .caseId(caseId.toString())
            .caseTypeId(CASE_TYPE)
            .caseData(caseData)
            .build();

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
    }*/
}

