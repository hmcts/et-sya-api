package uk.gov.hmcts.reform.et.syaapi;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.http.Header;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import uk.gov.hmcts.reform.et.syaapi.models.CaseRequest;
import uk.gov.hmcts.reform.et.syaapi.models.SubmitStoredApplicationRequest;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.hamcrest.Matchers.equalTo;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.IN_PROGRESS;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.OPEN_STATE;

@Slf4j
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class StoreCaseControllerFunctionalTest extends FunctionalTestBase {

    public static final String CASES_SUBMIT_STORED_CLAIMANT_APPLICATION = "/submit-stored-claimant-application";
    public static final String INVALID_TOKEN = "invalid_token";
    private static final Long CASE_ID = 12_345_678L;
    private static final String CASE_TYPE = "ET_EnglandWales";
    private static final String AUTHORIZATION = "Authorization";
    private static final String APP_ID = "987654321";
    private final Map<String, Object> caseData = new ConcurrentHashMap<>();

    @Test
    void submitStoredClaimantApplicationShouldReturnCaseDetails() {
        SubmitStoredApplicationRequest submitStoredApplicationRequest = SubmitStoredApplicationRequest.builder()
            .caseId(CASE_ID.toString())
            .caseTypeId(CASE_TYPE)
            .applicationId(APP_ID)
            .build();

        RestAssured.given()
            .contentType(ContentType.JSON)
            .header(new Header(AUTHORIZATION, userToken))
            .body(submitStoredApplicationRequest)
            .put(CASES_SUBMIT_STORED_CLAIMANT_APPLICATION)
            .then()
            .statusCode(HttpStatus.SC_OK)
            .log().all(true)
            .assertThat().body("id", equalTo(CASE_ID))
            .assertThat().body("case_data.genericTseApplicationCollection[0].value.applicationState",
                               equalTo(IN_PROGRESS))
            .assertThat().body("case_data.genericTseApplicationCollection[0].value.status",
                               equalTo(OPEN_STATE))
            .extract().body().jsonPath();
    }

    @Test
    void submitStoredClaimantApplicationWithInvalidAuthTokenShouldReturn403() {
        CaseRequest caseRequest = CaseRequest.builder()
            .caseData(caseData)
            .build();

        RestAssured.given()
            .contentType(ContentType.JSON)
            .header(new Header(AUTHORIZATION, INVALID_TOKEN))
            .body(caseRequest)
            .put(CASES_SUBMIT_STORED_CLAIMANT_APPLICATION)
            .then()
            .statusCode(HttpStatus.SC_FORBIDDEN)
            .log().all(true)
            .extract().body().jsonPath();
    }
}

