package uk.gov.hmcts.reform.et.syaapi.enums;

import uk.gov.hmcts.reform.et.syaapi.constants.ClaimTypesConstants;
import uk.gov.hmcts.reform.et.syaapi.service.utils.PdfMapperConstants;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public enum DiscriminationType {
    AGE(ClaimTypesConstants.AGE, PdfMapperConstants.Q8_TYPE_OF_DISCRIMINATION_AGE),
    DISABILITY(ClaimTypesConstants.DISABILITY, PdfMapperConstants.Q8_TYPE_OF_DISCRIMINATION_DISABILITY),
    ETHNICITY(ClaimTypesConstants.ETHNICITY, PdfMapperConstants.Q8_TYPE_OF_DISCRIMINATION_RACE),
    RACE(ClaimTypesConstants.RACE, PdfMapperConstants.Q8_TYPE_OF_DISCRIMINATION_RACE),
    GENDER_REASSIGNMENT(ClaimTypesConstants.GENDER_REASSIGNMENT,
                        PdfMapperConstants.Q8_TYPE_OF_DISCRIMINATION_GENDER_REASSIGNMENT),
    MARRIAGE_OR_CIVIL_PARTNERSHIP(ClaimTypesConstants.MARRIAGE_OR_CIVIL_PARTNERSHIP,
                                  PdfMapperConstants.Q8_TYPE_OF_DISCRIMINATION_MARRIAGE_OR_CIVIL_PARTNERSHIP),
    PREGNANCY_OR_MATERNITY(ClaimTypesConstants.PREGNANCY_OR_MATERNITY,
                           PdfMapperConstants.Q8_TYPE_OF_DISCRIMINATION_PREGNANCY_OR_MATERNITY),
    RELIGION_OR_BELIEF(ClaimTypesConstants.RELIGION_OR_BELIEF,
                       PdfMapperConstants.Q8_TYPE_OF_DISCRIMINATION_RELIGION_OR_BELIEF),
    SEX(ClaimTypesConstants.SEX, PdfMapperConstants.Q8_TYPE_OF_DISCRIMINATION_SEX),
    SEXUAL_ORIENTATION(ClaimTypesConstants.SEXUAL_ORIENTATION,
                       PdfMapperConstants.Q8_TYPE_OF_DISCRIMINATION_SEXUAL_ORIENTATION);

    private static final Map<String, DiscriminationType> BY_LABEL = new ConcurrentHashMap<>();

    static {
        for (DiscriminationType dt : values()) {
            BY_LABEL.put(dt.label, dt);
        }
    }

    public final String label;
    public final String pdfFieldName;

    DiscriminationType(String label, String pdfFieldName) {
        this.label = label;
        this.pdfFieldName = pdfFieldName;
    }

    public static DiscriminationType valueOfLabel(String label) {
        if (BY_LABEL.containsKey(label)) {
            return BY_LABEL.get(label);
        }
        return null;
    }
}
