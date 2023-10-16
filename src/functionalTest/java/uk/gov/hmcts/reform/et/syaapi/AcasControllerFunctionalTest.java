package uk.gov.hmcts.reform.et.syaapi;

import io.restassured.RestAssured;
import io.restassured.http.Header;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;

import java.util.List;

class AcasControllerFunctionalTest extends FunctionalTestBase {
    private static final String AUTHORIZATION = "Authorization";

    @Test
    void shouldReceiveAcceptedStatusWhenGetLastModifiedCaseInvoked() {
        RestAssured.given()
            .header(new Header(AUTHORIZATION, userToken))
            .param("datetime", "2022-11-23T00:00:00")
            .when()
            .get("/getLastModifiedCaseList")
            .then()
            .statusCode(HttpStatus.SC_OK)
            .log()
            .all(true);
    }

    @Test
    void shouldReceiveAcceptedStatusWhenGetCaseDataInvoked() {
        RestAssured.given()
            .header(new Header(AUTHORIZATION, userToken))
            .param("caseIds", List.of("1669137978672616"))
            .when()
            .get("/getCaseData")
            .then()
            .statusCode(HttpStatus.SC_OK)
            .log()
            .all(true);
    }
}
