package uk.gov.hmcts.reform.et.syaapi;

import io.restassured.RestAssured;
import io.restassured.http.Header;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;

class DocumentControllerFunctionalTest extends FunctionalTestBase {
    private static final String CASE_TYPE = "ET_EnglandWales";
    private static final String AUTHORIZATION = "Authorization";
    private static final File UPLOAD_TEST_FILE = new File("src/functionalTest/resources/uploads/zipcode.txt");

    @Test
    void shoudGetUploadedDocumentBinaryContent() {
        String retrievedDoc = RestAssured.given()
            .header(new Header(AUTHORIZATION, userToken))
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .when()
            .get("/document/download/" + retrieveUploadedFileId())
            .then()
            .statusCode(HttpStatus.SC_OK)
            .log().all(true)
            .extract().asString();

        assertThat(retrievedDoc).isNotNull();
        assertThat(retrievedDoc).contains("LS11AA");
    }

    @Test
    void shoudGetUploadedDocumentDetails() {
        RestAssured.given()
            .header(new Header(AUTHORIZATION, userToken))
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .when()
            .get("/document/details/" + retrieveUploadedFileId())
            .then()
            .statusCode(HttpStatus.SC_OK)
            .log().all(true)
            .assertThat().body("mimeType", equalTo("text/plain"))
            .assertThat().body("originalDocumentName", equalTo(UPLOAD_TEST_FILE.getName()));
    }

    private String retrieveUploadedFileId() {
        String uploadedDocUri = RestAssured.given()
            .header(new Header(AUTHORIZATION, userToken))
            .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
            .multiPart("document_upload", UPLOAD_TEST_FILE, MediaType.APPLICATION_JSON_VALUE)
            .when()
            .post("/documents/upload/" + CASE_TYPE)
            .then()
            .statusCode(HttpStatus.SC_OK)
            .extract()
            .body()
            .jsonPath()
            .get("uri");

        uploadedDocUri = uploadedDocUri.replaceAll(".*documents\\/", "");
        return uploadedDocUri;
    }
}
