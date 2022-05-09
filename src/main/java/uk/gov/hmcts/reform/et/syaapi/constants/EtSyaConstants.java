package uk.gov.hmcts.reform.et.syaapi.constants;

import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;

public final class EtSyaConstants {

    public static final String REMOTE_REPO = "https://github.com/hmcts/et-sya-api";
    public static final String JURISDICTION_ID = "EMPLOYMENT";
    public static final String SCOTLAND_CASE_TYPE = "ET_Scotland";
    public static final String DRAFT_EVENT_TYPE = "INITIATE_CASE_DRAFT";
    public static final int ZERO_INTEGER = 0;
    public static final String TEST_CASE_ID = "TEST_CASE_ID";
    public static final String AUTHORIZATION = "Authorization";
    public static final TribunalOffice DEFAULT_TRIBUNAL_OFFICE = TribunalOffice.LONDON_SOUTH;
    public static final String ELASTIC_SEARCH_STRING = "{\"match_all\": {}}";

    private EtSyaConstants() {
        // restrict instantiation
    }
}
