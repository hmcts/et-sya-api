package uk.gov.hmcts.reform.et.syaapi;

import io.restassured.RestAssured;
import io.restassured.http.Header;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.io.File;

import static org.hamcrest.Matchers.equalTo;

class DocumentUploadControllerFunctionalTest extends FunctionalTestBase {
    private static final String CASE_TYPE = "ET_EnglandWales";
    private static final String AUTHORIZATION = "Authorization";
    private static final File UPLOAD_TEST_FILE = new File("src/functionalTest/resources/uploads/zipcode.txt");

    @Test
    void uploadDocumentShouldReturnCaseDocument() {
        RestAssured.given()
            .header(new Header(AUTHORIZATION, userToken))
            .multiPart("document_upload", UPLOAD_TEST_FILE, MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
            .when()
            .post("/documents/upload/" + CASE_TYPE)
            .then()
            .statusCode(HttpStatus.SC_OK)
            .log().all(true)
            .assertThat().body("mimeType", equalTo("text/plain"))
            .assertThat().body("metadata.case_type_id", equalTo(CASE_TYPE));
    }

    @Test
    void uploadDocumentShouldReturnBadRequestIfCaseTypeIdDoesntMatch() {
        RestAssured.given()
            .header(new Header(AUTHORIZATION, userToken))
            .multiPart("document_upload", UPLOAD_TEST_FILE, MediaType.APPLICATION_JSON_VALUE)
            .when()
            .post("/documents/upload/WrongCaseTypeId")
            .then()
            .statusCode(HttpStatus.SC_BAD_REQUEST)
            .log().all(true);
    }
}
