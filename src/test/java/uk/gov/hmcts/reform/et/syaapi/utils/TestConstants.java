package uk.gov.hmcts.reform.et.syaapi.utils;

import uk.gov.service.notify.SendEmailResponse;

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

    private TestConstants() {

    }
}

