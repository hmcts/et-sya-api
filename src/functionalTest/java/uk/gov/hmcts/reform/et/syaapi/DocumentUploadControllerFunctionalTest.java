package uk.gov.hmcts.reform.et.syaapi;

import io.restassured.RestAssured;
import io.restassured.http.Header;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;


import static org.hamcrest.Matchers.equalTo;

@Slf4j
@TestMethodOrder(MethodOrderer.MethodName.class)
class DocumentUploadControllerFunctionalTest extends BaseFunctionalTest {
    private static final String CASE_TYPE = "ET_EnglandWales";
    private static final String AUTHORIZATION = "Authorization";
    private static final String DOCUMENT_NAME = "hello.txt";
    private static final String MOCK_FILE_BODY = "Hello, World!";
    private static final MockMultipartFile MOCK_FILE = new MockMultipartFile(
        "document_upload",
        DOCUMENT_NAME,
        MediaType.TEXT_PLAIN_VALUE,
        MOCK_FILE_BODY.getBytes()
    );

    @Test
    void uploadDocumentShouldReturnCaseDocument() {
        RestAssured.given()
            .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
            .header(new Header(AUTHORIZATION, userToken))
            .multiPart("document_upload", MOCK_FILE, MediaType.TEXT_PLAIN_VALUE)
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
            .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
            .header(new Header(AUTHORIZATION, userToken))
            .multiPart("document_upload", MOCK_FILE, MediaType.TEXT_PLAIN_VALUE)
            .post("/documents/upload/WrongCaseTypeId")
            .then()
            .statusCode(HttpStatus.SC_BAD_REQUEST)
            .log().all(true);
    }
}
