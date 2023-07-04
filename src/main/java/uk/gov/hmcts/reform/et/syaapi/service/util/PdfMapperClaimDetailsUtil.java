package uk.gov.hmcts.reform.et.syaapi.service.util;

import org.apache.commons.lang3.StringUtils;
import org.springframework.util.ObjectUtils;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.reform.et.syaapi.enums.ClaimType;
import uk.gov.hmcts.reform.et.syaapi.enums.DiscriminationType;
import uk.gov.hmcts.reform.et.syaapi.enums.PayClaimType;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentMap;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;
import static uk.gov.hmcts.reform.et.syaapi.constants.ClaimTypesConstants.DISCRIMINATION;
import static uk.gov.hmcts.reform.et.syaapi.constants.ClaimTypesConstants.OTHER_TYPES;
import static uk.gov.hmcts.reform.et.syaapi.constants.ClaimTypesConstants.PAY_RELATED_CLAIM;
import static uk.gov.hmcts.reform.et.syaapi.constants.ClaimTypesConstants.REDUNDANCY_PAY;

public final class PdfMapperClaimDetailsUtil {

    private PdfMapperClaimDetailsUtil() {
        // Utility classes should not have a public or default constructor.
    }

    public static void putClaimDetails(CaseData caseData, ConcurrentMap<String, Optional<String>> printFields) {
        try {
            if (ObjectUtils.isEmpty(caseData) || ObjectUtils.isEmpty(caseData.getTypesOfClaim())) {
                return;
            }
            putClaimTypes(printFields, caseData);
        } catch (Exception e) {
            GenericServiceUtil.logException("An error occurred while printing claim details to pdf file",
                                            caseData.getEthosCaseReference(),
                                            e.getMessage(),
                                            "PdfMapperClaimDetailsUtil",
                                            "putClaimDetails");
        }
    }

    private static void putClaimTypes(ConcurrentMap<String, Optional<String>> printFields, CaseData caseData) {
        for (String typeOfClaim : caseData.getTypesOfClaim()) {
            ClaimType claimType = ClaimType.valueOfLabel(typeOfClaim);
            if (claimType != null) {
                if (PAY_RELATED_CLAIM.equals(typeOfClaim)) {
                    putPayClaims(printFields, caseData);
                    continue;
                }
                printFields.put(claimType.pdfFieldName, Optional.of(YES));
                if (DISCRIMINATION.equals(typeOfClaim)) {
                    putDiscriminationClaims(printFields, caseData);
                }
                if (OTHER_TYPES.equals(typeOfClaim)) {
                    putOtherTypeOfClaim(printFields, caseData);
                }
            }
        }
    }

    private static void putDiscriminationClaims(ConcurrentMap<String, Optional<String>> printFields,
        CaseData caseData) {
        if (ObjectUtils.isEmpty(caseData) || ObjectUtils.isEmpty(caseData.getClaimantRequests())
            || ObjectUtils.isEmpty(caseData.getClaimantRequests().getDiscriminationClaims())) {
            return;
        }
        for (String discriminationType : caseData.getClaimantRequests().getDiscriminationClaims()) {
            DiscriminationType discriminationTypeEnum = DiscriminationType.valueOfLabel(discriminationType);
            if (discriminationTypeEnum != null) {
                printFields.put(discriminationTypeEnum.pdfFieldName, Optional.of(YES));
            }
        }
    }

    private static void putPayClaims(ConcurrentMap<String, Optional<String>> printFields, CaseData caseData) {
        if (ObjectUtils.isEmpty(caseData) || ObjectUtils.isEmpty(caseData.getClaimantRequests())
            || ObjectUtils.isEmpty(caseData.getClaimantRequests().getPayClaims())) {
            return;
        }
        for (String payClaimType : caseData.getClaimantRequests().getPayClaims()) {
            PayClaimType payClaimTypeEnum = PayClaimType.valueOfLabel(payClaimType);
            if (payClaimTypeEnum != null) {
                printFields.put(payClaimTypeEnum.pdfFieldName, Optional.of(YES));
                if (!REDUNDANCY_PAY.equals(payClaimType)) {
                    checkIAmOwedBox(printFields);
                }
            }
        }
    }

    private static void putOtherTypeOfClaim(ConcurrentMap<String, Optional<String>> printFields, CaseData caseData) {
        if (ObjectUtils.isEmpty(caseData)
            || ObjectUtils.isEmpty(caseData.getClaimantRequests())
            || StringUtils.isBlank(caseData.getClaimantRequests().getOtherClaim())) {
            return;
        }
        printFields.put(
            PdfMapperConstants.Q8_ANOTHER_TYPE_OF_CLAIM_TEXT_AREA,
            ofNullable(caseData.getClaimantRequests().getOtherClaim())
        );
    }

    private static void checkIAmOwedBox(Map<String, Optional<String>> printFields) {
        printFields.computeIfAbsent(PdfMapperConstants.Q8_TYPE_OF_CLAIM_I_AM_OWED, key -> Optional.of(YES));
    }
}
