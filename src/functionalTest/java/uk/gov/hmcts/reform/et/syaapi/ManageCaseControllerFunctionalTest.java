package uk.gov.hmcts.reform.et.syaapi;

import static io.restassured.RestAssured.baseURI;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.http.Header;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.RequestBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static io.restassured.RestAssured.useRelaxedHTTPSValidation;
import static org.hamcrest.Matchers.equalTo;

@Slf4j
public class ManageCaseControllerFunctionalTest extends BaseFunctionalTest{

    public ManageCaseControllerFunctionalTest() {
        baseURI = baseUrl;
        useRelaxedHTTPSValidation();
    }

    private RequestBuilder addHeaders(RequestBuilder requestBuilder) {
        return requestBuilder
            .setHeader(HttpHeaders.AUTHORIZATION, userToken);
    }

    @Test
    public void createCase_shouldReturnCaseData() throws Exception {
        RestAssured.given()
            .contentType(ContentType.JSON)
            .header(new Header("Authorization", userToken))
            .body(resourceAsString(
                "requests/caseData.json"
            ))
            .post("/case-type/ET_EnglandWales/event-type/INITIATE_CASE_DRAFT/case")
            .then()
            .statusCode(HttpStatus.SC_OK)
            .log().all(true)
            .assertThat().body("case_type_id", equalTo("ET_EnglandWales"));
    }

//    @Test
//    public void getCaseDetails_shouldReturnCaseDetails() throws Exception {
//        RestAssured.given()
//            .contentType(ContentType.JSON)
//            .header(new Header("Authorization", userToken))
//            .body(BaseHandler.getJsonCallbackForTest("handlers/interloc/dwpChallengeValidityCallback.json"))
//            .post("/caseDetails/1234")
//            .then()
//            .statusCode(HttpStatus.SC_OK)
//            .log().all(true)
//            .assertThat().body("data.interlocReviewState", equalTo("reviewByTcw"));
//    }

    public static String resourceAsString(final String resourcePath) throws IOException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        final File file = ResourceUtils.getFile(classLoader.getResource(resourcePath).getFile());
        return new String(Files.readAllBytes(file.toPath()));
    }

}
