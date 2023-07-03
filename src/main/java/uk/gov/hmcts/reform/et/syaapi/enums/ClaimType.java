package uk.gov.hmcts.reform.et.syaapi.enums;

import uk.gov.hmcts.reform.et.syaapi.constants.ClaimTypesConstants;
import uk.gov.hmcts.reform.et.syaapi.service.util.PdfMapperConstants;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public enum ClaimType {

    DISCRIMINATION(ClaimTypesConstants.DISCRIMINATION, PdfMapperConstants.Q8_TYPE_OF_CLAIM_DISCRIMINATION),
    PAY_RELATED(ClaimTypesConstants.PAY_RELATED_CLAIM, null),
    UNFAIR_DISMISSAL(ClaimTypesConstants.UNFAIR_DISMISSAL, PdfMapperConstants.Q8_TYPE_OF_CLAIM_UNFAIRLY_DISMISSED),
    WHISTLE_BLOWING(ClaimTypesConstants.WHISTLE_BLOWING, PdfMapperConstants.Q8_TYPE_OF_CLAIM_WHISTLE_BLOWING),
    OTHER_TYPES_OF_CLAIMS(ClaimTypesConstants.OTHER_TYPES, PdfMapperConstants.Q8_TYPE_OF_CLAIM_OTHER_TYPES_OF_CLAIMS);

    private static final Map<String, ClaimType> BY_LABEL = new ConcurrentHashMap<>();

    static {
        for (ClaimType ct : values()) {
            BY_LABEL.put(ct.label, ct);
        }
    }

    public final String label;
    public final String pdfFieldName;

    ClaimType(String label, String pdfFieldName) {
        this.label = label;
        this.pdfFieldName = pdfFieldName;
    }

    public static ClaimType valueOfLabel(String label) {
        if (BY_LABEL.containsKey(label)) {
            return BY_LABEL.get(label);
        }
        return null;
    }
}
