package uk.gov.hmcts.reform.et.syaapi.service.utils;

import org.json.JSONObject;
import uk.gov.hmcts.ecm.common.service.pdf.PdfDecodedMultipartFile;
import uk.gov.hmcts.et.common.model.ccd.types.DocumentType;
import uk.gov.hmcts.et.common.model.ccd.types.UploadedDocumentType;
import uk.gov.service.notify.SendEmailResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("PMD.TestClassWithoutTestCases")
public final class TestConstants {

    public static final String TEST_SERVICE_AUTH_TOKEN = "Bearer TestServiceAuth";
    public static final String UPDATE_CASE_DRAFT = "UPDATE_CASE_DRAFT";
    public static final String SUBMIT_CASE_DRAFT = "SUBMIT_CASE_DRAFT";
    public static final String INITIATE_CASE_DRAFT = "INITIATE_CASE_DRAFT";
    public static final String DRAFT = "DRAFT";
    public static final String AWAITING_SUBMISSION_TO_HMCTS = "AWAITING_SUBMISSION_TO_HMCTS";
    public static final String SUBMITTED = "SUBMITTED";
    public static final String TEST_STRING = "TEST";
    public static final String TYPE_OF_CLAIM_BREACH_OF_CONTRACT = "breachOfContract";
    public static final String TYPE_OF_CLAIM_DISCRIMINATION = "discrimination";
    public static final String TYPE_OF_CLAIM_PAY_RELATED_CLAIM = "payRelated";
    public static final String TYPE_OF_CLAIM_UNFAIR_DISMISSAL = "unfairDismissal";
    public static final String TYPE_OF_CLAIM_WHISTLE_BLOWING = "whistleBlowing";
    public static final String USER_ID = "TEST_USER_ID";
    public static final UUID NOTIFICATION_CONFIRMATION_ID = UUID.fromString("f30b2148-b1a6-4c0d-8a10-50109c96dc2c");
    public static final String DOC_UPLOAD_ERROR_EMAIL_TEMPLATE_ID = "3007a1e9-13b0-4bf9-9753-398ea91b8564";
    public static final String TEST_FIRST_NAME = "Joe";
    public static final String TEST_SURNAME = "Bloggs";
    public static final String TEST_NAME = "Name";
    public static final String CASE_ID = "TEST_CASE_ID";
    public static final String TEST_EMAIL = "TEST@GMAIL.COM";
    public static final String TEST_PDF_FILE_ORIGINAL_NAME = "Test pdf file original name";
    public static final String TEST_PDF_FILE_CONTENT_TYPE = "application/pdf";
    public static final String TEST_PDF_FILE_DOCUMENT_DESCRIPTION = "Test pdf file docuent description";
    public static final String TEST_TEMPLATE_API_KEY =
        "mtd_test-002d2170-e381-4545-8251-5e87dab724e7-ac8ef473-1f28-4bfc-8906-9babd92dc5d8";
    public static final String SUBMIT_CASE_CONFIRMATION_EMAIL_TEMPLATE_ID = "af0b26b7-17b6-4643-bbdc-e296d11e7b0c";
    public static final String WELSH_DUMMY_PDF_TEMPLATE_ID = "1234_welsh";
    public static final String UUID_DUMMY_STRING = "8835039a-3544-439b-a3da-882490d959eb";
    public static final String REFERENCE_STRING = "TEST_EMAIL_ALERT";
    public static final String TEST_SUBMIT_CASE_PDF_FILE_RESPONSE = "Dear test, Please see your detail "
        + "as 123456789. Regards, ET Team.";
    public static final String SEND_NOTIFICATION_ENGLISH_RESPONSE_BODY = "Please click here. "
        + "https://www.gov.uk/log-in-register-hmrc-online-services/123456722/?lng=en.";
    public static final String SEND_NOTIFICATION_WELSH_RESPONSE_BODY = "Please click here. "
        + "https://www.gov.uk/log-in-register-hmrc-online-services/123456722/?lng=cy.";
    public static final String SEND_NOTIFICATION_NO_LANGUAGE_RESPONSE_BODY = "Dear test, "
        + "Please see your detail as 123456789. Regards, ET Team.";
    public static final String UPLOADED_DOCUMENT_NAME = "Uploaded Document Name";
    public static final String UPLOADED_DOCUMENT_BINARY_URL = "https://uploaded.document.binary.url";
    public static final String UPLOADED_DOCUMENT_URL = "https://uploaded.document.url";
    public static final String FILE_NOT_EXISTS = "File does not exist!...";
    public static final String WELSH_LANGUAGE = "Welsh";
    public static final String ENGLISH_LANGUAGE = "English";
    public static final String EMPTY_STRING = "";
    public static final String NULL_STRING = null;
    public static final String CLAIMANT_IND_TYPE_NULL = "null";
    public static final String CLAIMANT_IND_TYPE_EMPTY = "empty";
    public static final String CLAIMANT_IND_TYPE_FILLED = "filled";
    public static final String INVALID_LANGUAGE = "Invalid Language";
    public static final boolean TRUE = true;
    public static final boolean FALSE = false;
    public static final String YES = "Yes";
    public static final String NO = "No";
    public static final byte[] EMPTY_BYTE_ARRAY = new byte[0];
    public static final String TEST_CASE_DATA_JSON_FILE = "requests/caseData.json";
    public static final PdfDecodedMultipartFile PDF_DECODED_MULTIPART_FILE_NULL = new PdfDecodedMultipartFile(
        null,
        TestConstants.TEST_PDF_FILE_ORIGINAL_NAME,
        TestConstants.TEST_PDF_FILE_CONTENT_TYPE,
        TestConstants.TEST_PDF_FILE_DOCUMENT_DESCRIPTION
    );

    public static final PdfDecodedMultipartFile PDF_DECODED_MULTIPART_FILE_EMPTY = new PdfDecodedMultipartFile(
        new byte[0],
        TestConstants.TEST_PDF_FILE_ORIGINAL_NAME,
        TestConstants.TEST_PDF_FILE_CONTENT_TYPE,
        TestConstants.TEST_PDF_FILE_DOCUMENT_DESCRIPTION
    );

    private static final String SEND_EMAIL_RESPONSE_BEGINNING = """
            {
              "id": "f30b2148-b1a6-4c0d-8a10-50109c96dc2c",
              "reference": "TEST_EMAIL_ALERT",
              "template": {
                "id": "8835039a-3544-439b-a3da-882490d959eb",
                "version": "3",
                "uri": "TEST"
              },
              "content": {
            """;
    public static final byte[] SAMPLE_BYTE_ARRAY = SEND_EMAIL_RESPONSE_BEGINNING.getBytes();
    private static final String SEND_EMAIL_RESPONSE_END = """
                "subject": "ET Test email created",
                "from_email": "TEST@GMAIL.COM"
              }
            }
            """;
    public static final SendEmailResponse INPUT_SEND_EMAIL_RESPONSE = new SendEmailResponse(
                        SEND_EMAIL_RESPONSE_BEGINNING
                        + "    \"body\": \"Dear test, Please see your detail as 123456789. Regards, ET Team.\",\n"
                        + SEND_EMAIL_RESPONSE_END);

    public static final SendEmailResponse SEND_EMAIL_RESPONSE_ENGLISH = new SendEmailResponse(
                        SEND_EMAIL_RESPONSE_BEGINNING
                        + "    \"body\": \"Please click here. https://www.gov.uk/"
                        + "log-in-register-hmrc-online-services/123456722/?lng=en.\",\n"
                        + SEND_EMAIL_RESPONSE_END);
    public static final SendEmailResponse SEND_EMAIL_RESPONSE_WELSH = new SendEmailResponse(
                        SEND_EMAIL_RESPONSE_BEGINNING
                        + "    \"body\": \"Please click here. https://www.gov.uk/"
                        + "log-in-register-hmrc-online-services/123456722/?lng=cy.\",\n"
                        + SEND_EMAIL_RESPONSE_END);
    public static final SendEmailResponse SEND_EMAIL_RESPONSE_DOC_UPLOAD_FAILURE = new SendEmailResponse("{\n"
                      + "  \"id\": \"f30b2148-b1a6-4c0d-8a10-50109c96dc2c\",\n"
                      + "  \"reference\": \"TEST_EMAIL_ALERT\",\n"
                      + "  \"template\": {\n"
                      + "    \"id\": \""
                      + DOC_UPLOAD_ERROR_EMAIL_TEMPLATE_ID
                      + "\",\n"
                      + "    \"version\": \"3\",\n"
                      + "    \"uri\": \"TEST\"\n"
                      + "  },\n"
                      + "  \"content\": {\n"
                      + "    \"body\": \"Dear test, "
                      + "Please see your detail as 123456789. Regards, "
                      + "ET Team.\",\n"
                      + "    \"subject\": \"ET Test email created\",\n"
                      + "    \"from_email\": \"" + TEST_EMAIL + "\"\n"
                      + "  }\n"
                      + "}\n");
    public static final String EMPTY_RESPONSE = "Empty Response";
    public static final List<PdfDecodedMultipartFile> NULL_BYTE_ARRAY_PDF_DECODED_MULTIPART_FILE_LIST = List.of(
        PDF_DECODED_MULTIPART_FILE_NULL);
    public static final List<PdfDecodedMultipartFile> EMPTY_BYTE_ARRAY_PDF_DECODED_MULTIPART_FILE_LIST = List.of(
        PDF_DECODED_MULTIPART_FILE_EMPTY);
    public static final PdfDecodedMultipartFile PDF_DECODED_MULTIPART_FILE1 = new PdfDecodedMultipartFile(
        SEND_EMAIL_RESPONSE_BEGINNING.getBytes(),
        TestConstants.TEST_PDF_FILE_ORIGINAL_NAME,
        TestConstants.TEST_PDF_FILE_CONTENT_TYPE,
        TestConstants.TEST_PDF_FILE_DOCUMENT_DESCRIPTION
    );

    public static final PdfDecodedMultipartFile PDF_DECODED_MULTIPART_FILE2 = new PdfDecodedMultipartFile(
        SEND_EMAIL_RESPONSE_END.getBytes(),
        TestConstants.TEST_PDF_FILE_ORIGINAL_NAME,
        TestConstants.TEST_PDF_FILE_CONTENT_TYPE,
        TestConstants.TEST_PDF_FILE_DOCUMENT_DESCRIPTION
    );
    public static final List<PdfDecodedMultipartFile> NOT_EMPTY_BYTE_ARRAY_PDF_DECODED_MULTIPART_FILE_LIST = List.of(
        PDF_DECODED_MULTIPART_FILE1);
    public static final List<PdfDecodedMultipartFile> MULTIPLE_BYTE_ARRAY_PDF_DECODED_MULTIPART_FILE_LIST = List.of(
        PDF_DECODED_MULTIPART_FILE1, PDF_DECODED_MULTIPART_FILE2);
    public static final List<PdfDecodedMultipartFile> EMPTY_PDF_DECODED_MULTIPART_FILE_LIST = new ArrayList<>();
    public static final UploadedDocumentType NULL_UPLOADED_DOCUMENT_TYPE_FILE =
        generateUploadedDocumentTypeByParams(null, null, null);
    public static final UploadedDocumentType EMPTY_UPLOADED_DOCUMENT_TYPE_FILE =
        generateUploadedDocumentTypeByParams("", "", "");
    public static final UploadedDocumentType NOT_EMPTY_UPLOADED_DOCUMENT_TYPE_FILE =
        generateUploadedDocumentTypeByParams(UPLOADED_DOCUMENT_BINARY_URL,
                                             UPLOADED_DOCUMENT_URL,
                                             UPLOADED_DOCUMENT_NAME);
    public static final String TYPE_OF_DOCUMENT_ET1 = "ET1";
    public static final String TYPE_OF_DOCUMENT_ET1_VETTING = "ET1 Vetting";
    public static final String DOCUMENT_OWNER = "Test Owner";
    public static final Map<String, Object> NOT_EMPTY_UPLOADED_DOCUMENT_TYPE_RAW_HASHMAP =
        generateUploadedDocumentTypeLinkedHashMap();
    public static final Map<String, Object> NOT_EMPTY_ET1_DOCUMENT_TYPE_RAW_HASHMAP =
        generateDocumentTypeLinkedHashMapByDocumentType(TYPE_OF_DOCUMENT_ET1);
    public static final Map<String, Object> NOT_EMPTY_ET1_VETTING_DOCUMENT_TYPE_RAW_HASHMAP =
        generateDocumentTypeLinkedHashMapByDocumentType(TYPE_OF_DOCUMENT_ET1_VETTING);
    public static final DocumentType NOT_EMPTY_ET1_DOCUMENT_TYPE =
        generateDocumentTypeObjectByDocumentType(TYPE_OF_DOCUMENT_ET1);
    public static final DocumentType NOT_EMPTY_ET1_VETTING_DOCUMENT_TYPE =
        generateDocumentTypeObjectByDocumentType(TYPE_OF_DOCUMENT_ET1_VETTING);
    public static final UploadedDocumentType EMPTY_UPLOADED_DOCUMENT_TYPE = new UploadedDocumentType();
    public static final JSONObject PREPARE_PDF_UPLOAD_JSON_OBJECT = new JSONObject("{\"file\":\""
                                               + "RGVhciB0ZXN0LCBQbGVhc2Ugc2VlIHlvdXIgZGV0YWlsIGFzIDEyMzQ1Njc"
                                               + "4OS4gUmVnYXJkcywgRVQgVGVhbS4=\","
                                               + "\"confirm_email_before_download\":null,\"retention_period\""
                                               + ":null,\"is_csv\":false}");
    public static final String MICHAEL = "Michael";

    private TestConstants() {

    }

    private static UploadedDocumentType generateUploadedDocumentTypeByParams(String binaryUrl,
                                                                             String documentUrl,
                                                                             String fileName) {
        UploadedDocumentType uploadedDocumentType = new UploadedDocumentType();
        uploadedDocumentType.setDocumentBinaryUrl(binaryUrl);
        uploadedDocumentType.setDocumentUrl(documentUrl);
        uploadedDocumentType.setDocumentFilename(fileName);
        return uploadedDocumentType;
    }

    private static Map<String, Object> generateUploadedDocumentTypeLinkedHashMap() {
        Map<String, Object> object = new ConcurrentHashMap<>();
        object.put("document_url", NOT_EMPTY_UPLOADED_DOCUMENT_TYPE_FILE.getDocumentUrl());
        object.put("document_filename", NOT_EMPTY_UPLOADED_DOCUMENT_TYPE_FILE.getDocumentFilename());
        object.put("document_binary_url", NOT_EMPTY_UPLOADED_DOCUMENT_TYPE_FILE.getDocumentBinaryUrl());
        return object;
    }

    private static Map<String, Object> generateDocumentTypeLinkedHashMapByDocumentType(String documentType) {
        Map<String, Object> object = new ConcurrentHashMap<>();
        object.put("typeOfDocument", documentType);
        object.put("ownerDocument", DOCUMENT_OWNER);
        object.put("documentType", documentType);
        object.put("uploadedDocument", NOT_EMPTY_UPLOADED_DOCUMENT_TYPE_RAW_HASHMAP);
        return object;
    }

    private static DocumentType generateDocumentTypeObjectByDocumentType(String documentType) {
        return DocumentType.builder().typeOfDocument(documentType)
            .documentType(documentType)
            .ownerDocument(DOCUMENT_OWNER)
            .uploadedDocument(NOT_EMPTY_UPLOADED_DOCUMENT_TYPE_FILE).build();
    }
}

