package uk.gov.hmcts.reform.et.syaapi;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.http.Header;
import io.restassured.path.json.JsonPath;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.ClaimantIndType;
import uk.gov.hmcts.et.common.model.ccd.types.RespondentSumType;
import uk.gov.hmcts.et.common.model.ccd.types.TseRespondType;
import uk.gov.hmcts.et.common.model.ccd.types.citizenhub.ClaimantTse;
import uk.gov.hmcts.reform.et.syaapi.models.CaseRequest;
import uk.gov.hmcts.reform.et.syaapi.models.ChangeApplicationStatusRequest;
import uk.gov.hmcts.reform.et.syaapi.models.ClaimantApplicationRequest;
import uk.gov.hmcts.reform.et.syaapi.models.RespondToApplicationRequest;
import uk.gov.hmcts.reform.et.syaapi.models.TribunalResponseViewedRequest;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;

@SuppressWarnings("PMD.LawOfDemeter")
@Slf4j
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ManageCaseControllerFunctionalTest extends FunctionalTestBase {

    private Long caseId;
    private static final String CASE_TYPE = "ET_EnglandWales";
    private static final String CLAIMANT_EMAIL = "citizen-user-test@test.co.uk";
    private static final String AUTHORIZATION = "Authorization";
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Map<String, Object> caseData = new ConcurrentHashMap<>();
    private String appId;
    private String responseId;

    @Test
    @Order(1)
    void createCaseShouldReturnCaseData() {
        caseData.put("caseType", "Single");
        caseData.put("caseSource", "Manually Created");
        caseData.put("claimant", "claimant");
        caseData.put("receiptDate", "1970-04-02");

        ClaimantIndType claimantIndType = new ClaimantIndType();
        claimantIndType.setClaimantFirstNames("Boris");
        claimantIndType.setClaimantLastName("Johnson");
        caseData.put("claimantIndType", claimantIndType);
        caseData.put("respondentCollection", List.of(createRespondentType()));
        caseData.put("claimantType", Map.of("claimant_email_address", CLAIMANT_EMAIL));

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
    @Order(2)
    void getSingleCaseDetailsShouldReturnSingleCaseDetails() {
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
    @Order(3)
    void getAllCaseDetailsShouldReturnAllCaseDetails() {
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
    @Order(4)
    void updateCaseShouldReturnUpdatedDraftCaseDetails() {
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


    @Test
    @Order(5)
    void submitCaseShouldReturnSubmittedCaseDetails() {
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
    }

    @Test
    @Order(6)
    void updateHubLinksStatuses() {
        CaseRequest caseRequest = CaseRequest.builder()
            .caseId(caseId.toString())
            .caseTypeId(CASE_TYPE)
            .caseData(caseData)
            .build();

        RestAssured.given()
            .contentType(ContentType.JSON)
            .header(new Header(AUTHORIZATION, userToken))
            .body(caseRequest)
            .put("/cases/update-hub-links-statuses")
            .then()
            .statusCode(HttpStatus.SC_OK)
            .log().all(true)
            .assertThat().body("id", equalTo(caseId))
            .assertThat().body("state", equalTo("Submitted"));
    }

    @Test
    @Order(7)
    void submitClaimantApplication() {
        ClaimantTse claimantTse = new ClaimantTse();
        claimantTse.setContactApplicationType("withdraw");

        ClaimantApplicationRequest claimantApplicationRequest = ClaimantApplicationRequest.builder()
            .caseId(caseId.toString())
            .caseTypeId(CASE_TYPE)
            .claimantTse(claimantTse)
            .build();

        JsonPath body = RestAssured.given()
            .contentType(ContentType.JSON)
            .header(new Header(AUTHORIZATION, userToken))
            .body(claimantApplicationRequest)
            .put("/cases/submit-claimant-application")
            .then()
            .statusCode(HttpStatus.SC_OK)
            .log().all(true)
            .assertThat().body("id", equalTo(caseId))
            .assertThat().body("state", equalTo("Submitted")).extract().body().jsonPath();

        CaseData caseDataWithTse = objectMapper.convertValue(body.get("case_data"), CaseData.class);
        appId = caseDataWithTse.getGenericTseApplicationCollection().get(0).getId();
    }

    @Test
    @Order(8)
    void respondToApplication() {
        RespondToApplicationRequest respondToApplicationRequest = RespondToApplicationRequest.builder()
            .caseId(caseId.toString())
            .caseTypeId(CASE_TYPE)
            .applicationId(appId)
            .response(new TseRespondType())
            .build();

        RestAssured.given()
            .contentType(ContentType.JSON)
            .header(new Header(AUTHORIZATION, userToken))
            .body(respondToApplicationRequest)
            .put("/cases/respond-to-application")
            .then()
            .statusCode(HttpStatus.SC_OK)
            .log().all(true)
            .assertThat().body("id", equalTo(caseId))
            .assertThat().body("state", equalTo("Submitted"));
    }

    @Test
    @Order(9)
    void changeApplicationStatus() {
        ChangeApplicationStatusRequest changeApplicationStatusRequest = ChangeApplicationStatusRequest.builder()
            .caseId(caseId.toString())
            .caseTypeId(CASE_TYPE)
            .applicationId(appId)
            .newStatus("test")
            .build();

        JsonPath body = RestAssured.given()
            .contentType(ContentType.JSON)
            .header(new Header(AUTHORIZATION, userToken))
            .body(changeApplicationStatusRequest)
            .put("/cases/change-application-status")
            .then()
            .statusCode(HttpStatus.SC_OK)
            .log().all(true)
            .assertThat().body("id", equalTo(caseId))
            .assertThat().body("state", equalTo("Submitted")).extract().body().jsonPath();

        CaseData caseDataWithTse = objectMapper.convertValue(body.get("case_data"), CaseData.class);
        responseId = caseDataWithTse.getGenericTseApplicationCollection()
            .get(0).getValue().getRespondCollection().get(0).getId();
    }

    @Test
    @Order(10)
    void updateResponseAsViewed() {
        TribunalResponseViewedRequest tribunalResponseViewedRequest = TribunalResponseViewedRequest.builder()
            .caseId(caseId.toString())
            .caseTypeId(CASE_TYPE)
            .appId(appId)
            .responseId(responseId)
            .build();

        RestAssured.given()
            .contentType(ContentType.JSON)
            .header(new Header(AUTHORIZATION, userToken))
            .body(tribunalResponseViewedRequest)
            .put("/cases/tribunal-response-viewed")
            .then()
            .statusCode(HttpStatus.SC_OK)
            .log().all(true)
            .assertThat().body("id", equalTo(caseId))
            .assertThat().body("state", equalTo("Submitted"));
    }


    private RespondentSumTypeItem createRespondentType() {
        RespondentSumType respondentSumType = new RespondentSumType();
        respondentSumType.setRespondentName("Boris Johnson");
        RespondentSumTypeItem respondentSumTypeItem = new RespondentSumTypeItem();
        respondentSumTypeItem.setValue(respondentSumType);

        return respondentSumTypeItem;
    }
}

