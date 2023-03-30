package uk.gov.hmcts.reform.et.syaapi.utils;

import uk.gov.service.notify.SendEmailResponse;

import java.util.UUID;

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
    public static final String SUBMIT_CASE_EMAIL_TEMPLATE_ID = "4f4b378e-238a-46ed-ae1c-26b8038192f0";
    public static final String DOC_UPLOAD_ERROR_EMAIL_TEMPLATE_ID = "3007a1e9-13b0-4bf9-9753-398ea91b8564";
    public static final String CITIZEN_PORTAL_LINK = "https://www.gov.uk/log-in-register-hmrc-online-services";
    public static final UUID NOTIFICATION_CONFIRMATION_ID = UUID.fromString("f30b2148-b1a6-4c0d-8a10-50109c96dc2c");
    public static final String EMAIL_TEST_GMAIL_COM = "test@gmail.com";
    public static final String EMAIL_TEST_SERVICE_OWNER_GMAIL_COM = "test@gmail.com";
    public static final String TEST_FIRST_NAME = "Joe";
    public static final String TEST_SURNAME = "Bloggs";
    public static final String TEST_NAME = "Name";
    public static final String DUMMY_CASE_TYPE = "Dummy_Case_Type";
    public static final String CASE_ID = "TEST_CASE_ID";
    public static final String TEST_EMAIL = "TEST@GMAIL.COM";
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

    private TestConstants() {

    }
}

