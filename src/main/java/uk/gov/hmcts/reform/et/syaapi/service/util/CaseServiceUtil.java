package uk.gov.hmcts.reform.et.syaapi.service.util;

import uk.gov.hmcts.et.common.model.ccd.CaseData;

import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.ENGLISH_LANGUAGE;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.WELSH_LANGUAGE;

public final class CaseServiceUtil {

    private CaseServiceUtil() {

    }

    public static String findClaimantLanguage(CaseData caseData) {
        return caseData.getClaimantHearingPreference() != null
            && caseData.getClaimantHearingPreference().getContactLanguage() != null
            && WELSH_LANGUAGE.equals(caseData.getClaimantHearingPreference().getContactLanguage()) ? WELSH_LANGUAGE
            : ENGLISH_LANGUAGE;
    }

}
