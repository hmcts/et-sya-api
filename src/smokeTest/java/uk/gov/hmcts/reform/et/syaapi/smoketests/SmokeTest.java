package uk.gov.hmcts.reform.et.syaapi.smoketests;

import io.restassured.RestAssured;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;

import static org.hamcrest.Matchers.equalTo;

@ComponentScan("uk.gov.hmcts.reform.et.syaapi.smoketests")
@SpringBootTest
class SmokeTest {

    private static final String SYA_API_TEST_URL = System.getenv("SYA_API_TEST_URL");

    @Test
    void healthCheckTest() {
        RestAssured.baseURI = SYA_API_TEST_URL;
        RestAssured.useRelaxedHTTPSValidation();

        RestAssured.given()
            .get("/health")
            .then()
            .statusCode(HttpStatus.SC_OK)
            .log().all(true)
            .assertThat().body("status", equalTo("UP"));

    }
}
