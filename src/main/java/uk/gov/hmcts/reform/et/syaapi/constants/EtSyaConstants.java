package uk.gov.hmcts.reform.et.syaapi.constants;

import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;

import java.util.List;

/**
 * Defines attributes used by et-sya-api as constants.
 */

public final class EtSyaConstants {

    public static final String REMOTE_REPO = "https://github.com/hmcts/et-sya-api";
    public static final String JURISDICTION_ID = "EMPLOYMENT";
    public static final String SCOTLAND_CASE_TYPE = "ET_Scotland";
    public static final String ENGLAND_CASE_TYPE = "ET_EnglandWales";
    public static final String DRAFT_EVENT_TYPE = "INITIATE_CASE_DRAFT";
    public static final String AUTHORIZATION = "Authorization";
    public static final TribunalOffice DEFAULT_TRIBUNAL_OFFICE = TribunalOffice.LONDON_SOUTH;
    public static final String RESOURCE_NOT_FOUND = "Resource not found for case id %s, reason: %s";
    public static final String UNASSIGNED_OFFICE = "Unassigned";
    public static final String CASE_FIELD_MANAGING_OFFICE = "managingOffice";

    public static final String OTHER_TYPE_OF_DOCUMENT = "Other";
    public static final String CLAIMANT_CORRESPONDENCE_DOCUMENT = "Claimant correspondence";
    public static final String YES = "Yes";

    public static final String NO = "No";

    public static final String WELSH_LANGUAGE = "Welsh";
    public static final String ENGLISH_LANGUAGE = "English";
    public static final String WELSH_LANGUAGE_PARAM = "/?lng=cy";

    public static final List<String> ACAS_VISIBLE_DOCS = List.of("ET1", "ACAS Certificate", "Notice of a claim",
                                                                 "ET3", CLAIMANT_CORRESPONDENCE_DOCUMENT,
                                                                 "Respondent correspondence", "Notice of Hearing",
                                                                 "Tribunal correspondence",
                                                                 "Tribunal Order/Deposit Order",
                                                                 "Tribunal Judgment/Reasons", "ET1 Attachment",
                                                                 "ET3 Attachment");



    private EtSyaConstants() {
        // restrict instantiation
    }
}
