package uk.gov.hmcts.reform.et.syaapi.service.utils;

import org.json.JSONObject;
import uk.gov.hmcts.et.common.model.ccd.Address;
import uk.gov.hmcts.et.common.model.ccd.types.UploadedDocumentType;
import uk.gov.hmcts.reform.et.syaapi.service.pdf.PdfDecodedMultipartFile;
import uk.gov.service.notify.SendEmailResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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
    public static final String CY_APPLICATION_ACKNOWLEDGEMENT_NO_EMAIL_TEMPLATE_ID =
        "bf0b26b7-17b6-4643-bbdc-e296d11e7b0d";
    public static final String CY_APPLICATION_ACKNOWLEDGEMENT_YES_EMAIL_TEMPLATE_ID =
        "cf0b26b7-17b6-4643-bbdc-e296d11e7b0e";
    public static final String CY_APPLICATION_ACKNOWLEDGEMENT_TYPE_C_EMAIL_TEMPLATE_ID =
        "df0b26b7-17b6-4643-bbdc-e296d11e7b0f";
    public static final String APPLICATION_ACKNOWLEDGEMENT_NO_EMAIL_TEMPLATE_ID =
        "ef0b26b7-17b6-4643-bbdc-e296d11e7b0g";
    public static final String APPLICATION_ACKNOWLEDGEMENT_YES_EMAIL_TEMPLATE_ID =
        "ff0b26b7-17b6-4643-bbdc-e296d11e7b0h";
    public static final String APPLICATION_ACKNOWLEDGEMENT_TYPE_C_EMAIL_TEMPLATE_ID =
        "gf0b26b7-17b6-4643-bbdc-e296d11e7b0i";
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
    public static final String ADDRESS_LINE_1 = "AddressLine1";
    public static final String ADDRESS_LINE_2 = "AddressLine2";
    public static final String ADDRESS_LINE_3 = "AddressLine3";
    public static final String POST_TOWN = "PostTown";
    public static final String COUNTY = "County";
    public static final String COUNTRY = "Country";
    public static final String WORK_ADDRESS_LINE_1 = "WorkAddressLine1";
    public static final String WORK_ADDRESS_LINE_2 = "WorkAddressLine2";
    public static final String WORK_ADDRESS_LINE_3 = "WorkAddressLine3";
    public static final String WORK_POST_TOWN = "WorkPostTown";
    public static final String WORK_COUNTY = "WorkCounty";
    public static final String WORK_COUNTRY = "WorkCountry";
    public static final String WORK_POSTCODE = "SL6 2DE";
    public static final String TEST_COMPANY_NAME = "Test Company Name";
    public static final int INTEGER_NUMERIC_ONE = 1;
    public static final int INTEGER_NUMERIC_TWO = 2;
    public static final int INTEGER_NUMERIC_THREE = 3;
    public static final int INTEGER_NUMERIC_FOUR = 4;
    public static final int INTEGER_NUMERIC_FIVE = 5;
    public static final int INTEGER_NUMERIC_SIX = 6;
    public static final String STRING_NUMERIC_ONE = "1";
    public static final String STRING_NUMERIC_TWO = "2";
    public static final String STRING_NUMERIC_THREE = "3";
    public static final String STRING_NUMERIC_FOUR = "4";
    public static final String STRING_NUMERIC_FIVE = "5";
    public static final String STRING_NUMERIC_SIX = "6";
    public static final String POSTCODE = "SW1A 1AA";
    public static final String REPRESENTATIVE_NAME = "Michael Mercury";
    public static final String REPRESENTATIVE_REFERENCE = "Test Reference";
    public static final String REPRESENTATIVE_ORGANISATION = "Test Organisation";
    public static final String REPRESENTATIVE_PHONE_NUMBER = "03123456789";
    public static final String REPRESENTATIVE_MOBILE_NUMBER = "07444518903";
    public static final String REPRESENTATIVE_EMAIL = "test@gmail.com";
    public static final String EMPTY_STRING = "";
    public static final String BLANK_STRING = "   ";
    public static final String NULL_STRING = null;
    public static final Address NULL_ADDRESS = null;
    public static final String TEST_ACAS = "R123456/78/90";
    public static final String CLAIMANT_IND_TYPE_NULL = "null";
    public static final String CLAIMANT_IND_TYPE_EMPTY = "empty";
    public static final String CLAIMANT_IND_TYPE_FILLED = "filled";
    public static final String TEST_NAMES = "Test Names";
    public static final String INVALID_LANGUAGE = "Invalid Language";
    public static final boolean TRUE = true;
    public static final boolean FALSE = false;
    public static final String CLAIM_DESCRIPTION = "Test Claim Description";
    public static final String MR = "Mr";
    public static final String MS = "Ms";
    public static final String MRS = "Mrs";
    public static final String MISS = "Miss";
    public static final String ELIZABETH = "Elizabeth";
    public static final String TAYLOR = "Taylor";
    public static final String SWIFT = "Swift";
    public static final String PREFER_NOT_TO_SAY = "Prefer not to say";
    public static final String YES = "Yes";
    public static final String NO = "No";
    public static final String REASONABLE_ADJUSTMENT_DETAILS = "Wheelchair, Walking-Stick";
    public static final byte[] EMPTY_BYTE_ARRAY = new byte[0];
    public static final String[] EMPTY_STRING_ARRAY = new String[0];
    public static final String HEARING_ASSISTANCE = "Hearing Assistance";
    public static final String VIDEO = "Video";
    public static final String PHONE = "Phone";
    public static final String TEST_CASE_DATA_JSON_FILE = "requests/caseData.json";
    public static final String WEEKS = "Weeks";
    public static final String MONTHS = "Months";
    public static final String ANNUAL = "Annual";
    public static final String CLAIMANT_EMPLOYED_FROM = "2020-01-01";
    public static final String CLAIMANT_EMPLOYED_TO = "2022-01-01";
    public static final String TEST_NEW_COMPANY = "Test New Company";
    public static final String CLAIMANT_OCCUPATION = "IT Manager";
    public static final String AVERAGE_WEEKLY_HOURS = "37.5";
    public static final String CLAIMANT_PAY_BEFORE_TAX = "45000";
    public static final String CLAIMANT_PAY_AFTER_TAX = "40000";
    public static final String CLAIMANT_PENSION_WEEKLY_CONTRIBUTION = "50";
    public static final String CLAIMANT_BENEFITS_DETAIL = "Car";
    public static final String NEW_PAY_BEFORE_TAX = "60000";
    public static final String NO_LONGER_WORKING = "No longer working";
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

    private static final String SEND_EMAIL_RESPONSE_BEGINNING = "{\n"
        + "  \"id\": \"f30b2148-b1a6-4c0d-8a10-50109c96dc2c\",\n"
        + "  \"reference\": \"TEST_EMAIL_ALERT\",\n"
        + "  \"template\": {\n"
        + "    \"id\": \"8835039a-3544-439b-a3da-882490d959eb\",\n"
        + "    \"version\": \"3\",\n"
        + "    \"uri\": \"TEST\"\n"
        + "  },\n"
        + "  \"content\": {\n";
    private static final String SEND_EMAIL_RESPONSE_END = "    \"subject\": \"ET Test email created\",\n"
        + "    \"from_email\": \"TEST@GMAIL.COM\"\n"
        + "  }\n"
        + "}\n";
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
    public static final UploadedDocumentType EMPTY_UPLOADED_DOCUMENT_TYPE = new UploadedDocumentType();
    public static final JSONObject PREPARE_PDF_UPLOAD_JSON_OBJECT = new JSONObject("{\"file\":\""
                                               + "RGVhciB0ZXN0LCBQbGVhc2Ugc2VlIHlvdXIgZGV0YWlsIGFzIDEyMzQ1Njc"
                                               + "4OS4gUmVnYXJkcywgRVQgVGVhbS4=\","
                                               + "\"confirm_email_before_download\":null,\"retention_period\""
                                               + ":null,\"is_csv\":false}");
    public static final String CLAIMANT_PHONE_NUMBER_FIELD_NAME = "1.6 phone number";
    public static final String CLAIMANT_MOBILE_NUMBER_FIELD_NAME = "1.7 mobile number";
    public static final String CLAIMANT_EMAIL_FIELD_NAME = "1.9 email";
    public static final String CLAIMANT_CONTACT_EMAIL_FIELD_NAME = "1.8 How should we contact you - Email";
    public static final String CLAIMANT_CONTACT_POST_FIELD_NAME = "1.8 How should we contact you - Post";
    public static final String OTHER = "Other";
    public static final String OTHER_TITLE = "Other Title";
    public static final String MICHAEL = "Michael";
    public static final String MERCURY = "Mercury";
    public static final String MALE = "Male";
    public static final String FEMALE = "Female";
    public static final String EMAIL = "Email";
    public static final String POST = "Post";
    public static final String FAX = "Fax";

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
}

