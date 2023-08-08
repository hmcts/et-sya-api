package uk.gov.hmcts.reform.et.syaapi.enums;

import uk.gov.hmcts.reform.et.syaapi.constants.ClaimTypesConstants;
import uk.gov.hmcts.reform.et.syaapi.service.utils.PdfMapperConstants;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public enum PayClaimType {

    ARREARS(ClaimTypesConstants.ARREARS, PdfMapperConstants.Q8_TYPE_OF_PAY_CLAIMS_ARREARS),
    HOLIDAY_PAY(ClaimTypesConstants.HOLIDAY_PAY, PdfMapperConstants.Q8_TYPE_OF_PAY_CLAIMS_HOLIDAY_PAY),
    NOTICE_PAY(ClaimTypesConstants.NOTICE_PAY, PdfMapperConstants.Q8_TYPE_OF_PAY_CLAIMS_NOTICE_PAY),
    OTHER_PAYMENTS(ClaimTypesConstants.OTHER_PAYMENTS, PdfMapperConstants.Q8_TYPE_OF_PAY_CLAIMS_OTHER_PAYMENTS),
    REDUNDANCY_PAY(ClaimTypesConstants.REDUNDANCY_PAY, PdfMapperConstants.Q8_TYPE_OF_CLAIM_REDUNDANCY_PAYMENT);

    private static final Map<String, PayClaimType> BY_LABEL = new ConcurrentHashMap<>();

    static {
        for (PayClaimType pt : values()) {
            BY_LABEL.put(pt.label, pt);
        }
    }

    public final String label;
    public final String pdfFieldName;

    PayClaimType(String label, String pdfFieldName) {
        this.label = label;
        this.pdfFieldName = pdfFieldName;
    }

    public static PayClaimType valueOfLabel(String label) {
        if (BY_LABEL.containsKey(label)) {
            return BY_LABEL.get(label);
        }
        return null;
    }

}
