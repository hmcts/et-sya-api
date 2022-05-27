package uk.gov.hmcts.reform.et.syaapi.smoketests;

import io.restassured.RestAssured;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;

import static org.hamcrest.Matchers.equalTo;

@ComponentScan("uk.gov.hmcts.reform.et.syaapi.smoketests")
@SpringBootTest
class SmokeTest {

    @Value("${test.url}")
    protected String syaApiTestUrl;

    @Test
    void healthCheckTest() {
        RestAssured.baseURI = syaApiTestUrl;
        RestAssured.useRelaxedHTTPSValidation();

        RestAssured.given()
            .get("/health")
            .then()
            .statusCode(HttpStatus.SC_OK)
            .log().all(true)
            .assertThat().body("status", equalTo("UP"));

    }
}
