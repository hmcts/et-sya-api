package uk.gov.hmcts.reform.et.syaapi.utils;

import java.util.UUID;

public final class TestConstants {

    public static final String TEST_SERVICE_AUTH_TOKEN = "Bearer TestServiceAuth";
    public static final String INITIATE_CASE_DRAFT = "INITIATE_CASE_DRAFT";
    public static final String SUBMIT_CASE_DRAFT = "SUBMIT_CASE_DRAFT";
    public static final String UPDATE_CASE_DRAFT = "UPDATE_CASE_DRAFT";
    public static final String DRAFT = "DRAFT";
    public static final String AWAITING_SUBMISSION_TO_HMCTS = "AWAITING_SUBMISSION_TO_HMCTS";
    public static final String SUBMITTED = "SUBMITTED";
    public static final String TEST_STRING = "TEST";
    public static final String TYPE_OF_CLAIM_BREACH_OF_CONTRACT = "breachOfContract";
    public static final String TYPE_OF_CLAIM_DISCRIMINATION = "discrimination";
    public static final String TYPE_OF_CLAIM_PAY_RELATED_CLAIM = "payRelated";
    public static final String TYPE_OF_CLAIM_UNFAIR_DISMISSAL = "unfairDismissal";
    public static final String TYPE_OF_CLAIM_WHISTLE_BLOWING = "whistleBlowing";
    public static final String CASE_ID = "123456/2022";
    public static final String USER_ID = "TEST_USER_ID";
    public static final String SUBMIT_CASE_EMAIL_TEMPLATE_ID = "4f4b378e-238a-46ed-ae1c-26b8038192f0";
    public static final String CITIZEN_PORTAL_LINK = "https://www.gov.uk/log-in-register-hmrc-online-services";
    public static final UUID NOTIFICATION_CONFIRMATION_ID = UUID.fromString("f30b2148-b1a6-4c0d-8a10-50109c96dc2c");

    private TestConstants() {

    }
}

