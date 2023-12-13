package uk.gov.hmcts.reform.et.syaapi.constants;

import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;

import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Defines attributes used by et-sya-api as constants.
 */

public final class EtSyaConstants {

    public static final String AUTHORIZATION = "Authorization";
    public static final String CASE_FIELD_MANAGING_OFFICE = "managingOffice";
    public static final String CASE_ID_NOT_FOUND = "case id not found";
    public static final TribunalOffice DEFAULT_TRIBUNAL_OFFICE = TribunalOffice.LONDON_SOUTH;
    public static final String DRAFT_EVENT_TYPE = "INITIATE_CASE_DRAFT";
    public static final String ENGLAND_CASE_TYPE = "ET_EnglandWales";
    public static final String ENGLISH_LANGUAGE = "English";
    public static final String FILE_NOT_EXISTS = "File does not exist!...";
    public static final String JURISDICTION_ID = "EMPLOYMENT";
    public static final String ET1_ATTACHMENT = "ET1 Attachment";
    public static final String REMOTE_REPO = "https://github.com/hmcts/et-sya-api";
    public static final String RESOURCE_NOT_FOUND = "Resource not found for case id %s, reason: %s";
    public static final String SEND_EMAIL_PARAMS_ACAS_PDF1_LINK_KEY = "link_to_acas_cert_pdf_file_1";
    public static final String SEND_EMAIL_PARAMS_ACAS_PDF2_LINK_KEY = "link_to_acas_cert_pdf_file_2";
    public static final String SEND_EMAIL_PARAMS_ACAS_PDF3_LINK_KEY = "link_to_acas_cert_pdf_file_3";
    public static final String SEND_EMAIL_PARAMS_ACAS_PDF4_LINK_KEY = "link_to_acas_cert_pdf_file_4";
    public static final String SEND_EMAIL_PARAMS_ACAS_PDF5_LINK_KEY = "link_to_acas_cert_pdf_file_5";
    public static final String SEND_EMAIL_PARAMS_CASE_ID = "caseId";
    public static final String SEND_EMAIL_PARAMS_CASE_NUMBER_KEY = "caseNumber";
    public static final String SEND_EMAIL_PARAMS_CITIZEN_PORTAL_LINK_KEY = "citizenPortalLink";
    public static final String SEND_EMAIL_PARAMS_EXUI_LINK_KEY = "exuiCaseDetailsLink";
    public static final String SEND_EMAIL_PARAMS_CLAIM_DESCRIPTION_FILE_LINK_KEY = "link_to_claim_description_file";
    public static final String SEND_EMAIL_PARAMS_ET1PDF_ENGLISH_LINK_KEY = "link_to_et1_pdf_file_en";
    public static final String SEND_EMAIL_PARAMS_ET1PDF_LINK_KEY = "link_to_et1_pdf_file";
    public static final String SEND_EMAIL_PARAMS_ET1PDF_WELSH_LINK_KEY = "link_to_et1_pdf_file_cy";
    public static final String SEND_EMAIL_PARAMS_SUBJECTLINE_KEY = "subjectLine";
    public static final String SEND_EMAIL_PARAMS_FIRSTNAME_KEY = "firstName";
    public static final String SEND_EMAIL_PARAMS_LASTNAME_KEY = "lastName";
    public static final String SEND_EMAIL_PARAMS_HEARING_DATE_KEY = "hearingDate";
    public static final String SEND_EMAIL_PARAMS_SHORTTEXT_KEY = "shortText";
    public static final String SEND_EMAIL_PARAMS_DATEPLUS7_KEY = "datePlus7";
    public static final String SEND_EMAIL_PARAMS_LINK_DOC_KEY = "linkToDocument";
    public static final String SEND_EMAIL_SERVICE_OWNER_NAME_KEY = "serviceOwnerName";
    public static final String SEND_EMAIL_SERVICE_OWNER_NAME_VALUE = "Service Owner";
    public static final String SCOTLAND_CASE_TYPE = "ET_Scotland";
    public static final DateTimeFormatter UK_LOCAL_DATE_PATTERN = DateTimeFormatter.ofPattern("dd MMM yyyy");
    public static final String UNASSIGNED_OFFICE = "Unassigned";

    public static final String CLAIMANT_CORRESPONDENCE_DOCUMENT = "Claimant correspondence";
    public static final String YES = "Yes";

    public static final String WELSH_LANGUAGE = "Welsh";
    public static final String WELSH_LANGUAGE_PARAM = "/?lng=cy";

    public static final String ET3_ATTACHMENT = "ET3 Attachment";
    public static final String ET3 = "ET3";

    public static final String WELSH_LANGUAGE_PARAM_WITHOUT_FWDSLASH = "?lng=cy";

    public static final String ET1_ONLINE_SUBMISSION = "et1OnlineSubmission";

    public static final List<String> ACAS_VISIBLE_DOCS = List.of("ET1", "ACAS Certificate", "Notice of a claim",
                                                                 ET3, CLAIMANT_CORRESPONDENCE_DOCUMENT,
                                                                 "Respondent correspondence", "Notice of Hearing",
                                                                 "Tribunal correspondence",
                                                                 "Tribunal Order/Deposit Order",
                                                                 "Tribunal Judgment/Reasons", ET1_ATTACHMENT,
                                                                 ET3_ATTACHMENT);

    public static final List<String> POSSIBLE_DUPLICATED_DOCS = List.of(ET3, ET3_ATTACHMENT, ET1_ATTACHMENT);


    private EtSyaConstants() {
        // restrict instantiation
    }
}
