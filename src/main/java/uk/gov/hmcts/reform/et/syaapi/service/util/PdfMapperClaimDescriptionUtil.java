package uk.gov.hmcts.reform.et.syaapi.service.util;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import uk.gov.hmcts.et.common.model.ccd.CaseData;

import java.util.Optional;
import java.util.concurrent.ConcurrentMap;

import static java.util.Optional.ofNullable;

public final class PdfMapperClaimDescriptionUtil {

    private PdfMapperClaimDescriptionUtil() {
        // Utility classes should not have a public or default constructor.
    }

    public static void putClaimDescription(CaseData caseData, ConcurrentMap<String, Optional<String>> printFields) {
        if (ObjectUtils.isNotEmpty(caseData.getClaimantRequests())
            && StringUtils.isNotBlank(caseData.getClaimantRequests().getClaimDescription())) {
            printFields.put(PdfMapperConstants.Q8_CLAIM_DESCRIPTION,
                            ofNullable(caseData.getClaimantRequests().getClaimDescription()));
        }
    }

}
