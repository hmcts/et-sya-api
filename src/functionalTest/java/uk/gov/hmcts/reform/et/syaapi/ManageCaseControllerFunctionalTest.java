package uk.gov.hmcts.reform.et.syaapi;

import static io.restassured.RestAssured.baseURI;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.http.Header;
import io.restassured.path.json.JsonPath;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import uk.gov.hmcts.reform.et.syaapi.models.CaseRequest;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.useRelaxedHTTPSValidation;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;

@Slf4j
@TestMethodOrder(MethodOrderer.MethodName.class)
public class ManageCaseControllerFunctionalTest extends BaseFunctionalTest{

    private Long caseId;
    private Long secondCaseId;
    private String caseTypeEngland = "ET_EnglandWales";
    private String claimantEmail = "citizen-user-test@test.co.uk";

    public ManageCaseControllerFunctionalTest() {
        baseURI = baseUrl;
        useRelaxedHTTPSValidation();
    }

    @Test
    public void stage1_createCase_shouldReturnCaseData() {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put("caseType", "Single");
        caseData.put("caseSource", "Manually Created");
        CaseRequest caseRequest = CaseRequest.builder()
            .caseData(caseData)
            .build();

        JsonPath body = RestAssured.given()
            .contentType(ContentType.JSON)
            .header(new Header("Authorization", userToken))
            .body(caseRequest)
            .post("/cases/initiate-case")
            .then()
            .statusCode(HttpStatus.SC_OK)
            .log().all(true)
            .extract().body().jsonPath();

        assertEquals(caseTypeEngland, body.get("case_type_id"));
        caseId = body.get("id");

        RestAssured.given()
            .contentType(ContentType.JSON)
            .header(new Header("Authorization", userToken))
            .body(caseRequest)
            .post("/cases/initiate-case")
            .then()
            .statusCode(HttpStatus.SC_OK)
            .log().all(true);
    }

    @Test
    public void stage2_getSingleCaseDetails_shouldReturnSingleCaseDetails() {
        RestAssured.given()
            .contentType(ContentType.JSON)
            .header(new Header("Authorization", userToken))
            .body("{\"case_id\":\"" + caseId + "\"}")
            .post("/cases/user-case")
            .then()
            .statusCode(HttpStatus.SC_OK)
            .log().all(true)
            .assertThat().body("id", equalTo(caseId))
            .assertThat().body("case_type_id", equalTo(caseTypeEngland));
    }

    @Test
    public void stage3_getAllCaseDetails_shouldReturnAllCaseDetails() {
        RestAssured.given()
            .contentType(ContentType.JSON)
            .header(new Header("Authorization", userToken))
            .body("{\"case_id\":\"" + caseId + "\"}")
            .get("/cases/user-cases")
            .then()
            .statusCode(HttpStatus.SC_OK)
            .log().all(true)
            .assertThat().body("size()", is(2));
    }

    @Test
    public void stage4_updateCase_shouldReturnUpdatedCaseDetails() {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put("claimantType", Map.of("claimant_email_address", claimantEmail));

        CaseRequest caseRequest = CaseRequest.builder()
            .caseId(caseId.toString())
            .caseTypeId(caseTypeEngland)
            .caseData(caseData)
            .build();

        RestAssured.given()
            .contentType(ContentType.JSON)
            .header(new Header("Authorization", userToken))
            .body(caseRequest)
            .put("/cases/update-case")
            .then()
            .statusCode(HttpStatus.SC_OK)
            .log().all(true)
            .assertThat().body("id", equalTo(caseId))
            .assertThat().body("case_data.claimantType.claimant_email_address", equalTo(claimantEmail));
    }

    @Test
    public void stage5_submitCase_shouldReturnSubmittedCaseDetails() {
        CaseRequest caseRequest = CaseRequest.builder()
            .caseId(caseId.toString())
            .caseTypeId(caseTypeEngland)
            .build();

        RestAssured.given()
            .contentType(ContentType.JSON)
            .header(new Header("Authorization", userToken))
            .body(caseRequest)
            .put("/cases/submit-case")
            .then()
            .statusCode(HttpStatus.SC_OK)
            .log().all(true)
            .assertThat().body("id", equalTo(caseId))
            .assertThat().body("state", equalTo("Submitted"));
    }


}
