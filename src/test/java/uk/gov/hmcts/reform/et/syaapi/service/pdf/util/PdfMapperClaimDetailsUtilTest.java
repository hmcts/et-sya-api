package uk.gov.hmcts.reform.et.syaapi.service.pdf.util;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;
import org.springframework.util.ObjectUtils;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.reform.et.syaapi.enums.ClaimType;
import uk.gov.hmcts.reform.et.syaapi.enums.DiscriminationType;
import uk.gov.hmcts.reform.et.syaapi.enums.PayClaimType;
import uk.gov.hmcts.reform.et.syaapi.service.util.PdfMapperClaimDetailsUtil;
import uk.gov.hmcts.reform.et.syaapi.service.util.PdfMapperConstants;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.et.syaapi.constants.ClaimTypesConstants.DISCRIMINATION;
import static uk.gov.hmcts.reform.et.syaapi.constants.ClaimTypesConstants.OTHER_TYPES;
import static uk.gov.hmcts.reform.et.syaapi.constants.ClaimTypesConstants.PAY_RELATED_CLAIM;
import static uk.gov.hmcts.reform.et.syaapi.constants.ClaimTypesConstants.REDUNDANCY_PAY;

class PdfMapperClaimDetailsUtilTest {

    @ParameterizedTest
    @NullSource
    @MethodSource("uk.gov.hmcts.reform.et.syaapi.service.util.data.PdfMapperClaimDetailsUtilTestDataProvider#"
        + "generateCaseDataSamplesForClaimDetailsUtilTest")
    void putClaimDetails(CaseData caseData) {
        ConcurrentMap<String, Optional<String>> printFields = new ConcurrentHashMap<>();
        PdfMapperClaimDetailsUtil.putClaimDetails(caseData, printFields);
        if (ObjectUtils.isEmpty(caseData) || ObjectUtils.isEmpty(caseData.getTypesOfClaim())) {
            assertThat(printFields.get(ClaimType.OTHER_TYPES_OF_CLAIMS.pdfFieldName)).isNull();
            assertThat(printFields.get(ClaimType.UNFAIR_DISMISSAL.pdfFieldName)).isNull();
            assertThat(printFields.get(ClaimType.WHISTLE_BLOWING.pdfFieldName)).isNull();
            assertThat(printFields.get(ClaimType.DISCRIMINATION.pdfFieldName)).isNull();
        } else {
            checkClaimTypes(printFields, caseData);
        }
    }

    private static void checkClaimTypes(ConcurrentMap<String, Optional<String>> printFields, CaseData caseData) {
        for (String typeOfClaim : caseData.getTypesOfClaim()) {
            ClaimType claimType = ClaimType.valueOfLabel(typeOfClaim);
            if (claimType != null) {
                if (PAY_RELATED_CLAIM.equals(typeOfClaim)) {
                    checkPayClaims(printFields, caseData);
                    continue;
                }
                assertThat(printFields.get(claimType.pdfFieldName)).contains(PdfMapperConstants.YES);
                if (DISCRIMINATION.equals(typeOfClaim)) {
                    checkDiscriminationClaims(printFields, caseData);
                }
                if (OTHER_TYPES.equals(typeOfClaim)) {
                    checkOtherTypeOfClaim(printFields, caseData);
                }
            }
        }
    }

    private static void checkPayClaims(ConcurrentMap<String, Optional<String>> printFields, CaseData caseData) {
        if (ObjectUtils.isEmpty(caseData.getClaimantRequests())
            || ObjectUtils.isEmpty(caseData.getClaimantRequests().getPayClaims())) {
            assertThat(printFields.get(PayClaimType.HOLIDAY_PAY.pdfFieldName)).isNull();
            assertThat(printFields.get(PayClaimType.REDUNDANCY_PAY.pdfFieldName)).isNull();
            assertThat(printFields.get(PayClaimType.NOTICE_PAY.pdfFieldName)).isNull();
            assertThat(printFields.get(PayClaimType.ARREARS.pdfFieldName)).isNull();
            assertThat(printFields.get(PayClaimType.OTHER_PAYMENTS.pdfFieldName)).isNull();
        } else {
            for (String payClaimType : caseData.getClaimantRequests().getPayClaims()) {
                PayClaimType payClaimTypeEnum = PayClaimType.valueOfLabel(payClaimType);
                if (payClaimTypeEnum != null) {
                    assertThat(printFields.get(payClaimTypeEnum.pdfFieldName)).contains(PdfMapperConstants.YES);
                    if (!REDUNDANCY_PAY.equals(payClaimType)) {
                        assertThat(printFields.get(PdfMapperConstants.Q8_TYPE_OF_CLAIM_I_AM_OWED))
                            .contains(PdfMapperConstants.YES);
                    }
                }
            }
        }
    }

    private static void checkDiscriminationClaims(ConcurrentMap<String, Optional<String>> printFields,
                                                CaseData caseData) {
        if (ObjectUtils.isEmpty(caseData) || ObjectUtils.isEmpty(caseData.getClaimantRequests())
            || ObjectUtils.isEmpty(caseData.getClaimantRequests().getDiscriminationClaims())) {
            assertThat(printFields.get(DiscriminationType.AGE.pdfFieldName)).isNull();
            assertThat(printFields.get(DiscriminationType.DISABILITY.pdfFieldName)).isNull();
            assertThat(printFields.get(DiscriminationType.SEX.pdfFieldName)).isNull();
            assertThat(printFields.get(DiscriminationType.ETHNICITY.pdfFieldName)).isNull();
            assertThat(printFields.get(DiscriminationType.GENDER_REASSIGNMENT.pdfFieldName)).isNull();
            assertThat(printFields.get(DiscriminationType.MARRIAGE_OR_CIVIL_PARTNERSHIP.pdfFieldName)).isNull();
            assertThat(printFields.get(DiscriminationType.RACE.pdfFieldName)).isNull();
            assertThat(printFields.get(DiscriminationType.PREGNANCY_OR_MATERNITY.pdfFieldName)).isNull();
            assertThat(printFields.get(DiscriminationType.RELIGION_OR_BELIEF.pdfFieldName)).isNull();
            assertThat(printFields.get(DiscriminationType.SEXUAL_ORIENTATION.pdfFieldName)).isNull();
        } else {
            for (String discriminationType : caseData.getClaimantRequests().getDiscriminationClaims()) {
                DiscriminationType discriminationTypeEnum = DiscriminationType.valueOfLabel(discriminationType);
                if (discriminationTypeEnum != null) {
                    assertThat(printFields.get(discriminationTypeEnum.pdfFieldName)).contains(PdfMapperConstants.YES);
                }
            }
        }
    }

    private static void checkOtherTypeOfClaim(ConcurrentMap<String, Optional<String>> printFields, CaseData caseData) {
        if (ObjectUtils.isEmpty(caseData)
            || ObjectUtils.isEmpty(caseData.getClaimantRequests())
            || StringUtils.isBlank(caseData.getClaimantRequests().getOtherClaim())) {
            assertThat(printFields.get(PdfMapperConstants.Q8_ANOTHER_TYPE_OF_CLAIM_TEXT_AREA)).isNull();
        } else {
            assertThat(printFields.get(PdfMapperConstants.Q8_ANOTHER_TYPE_OF_CLAIM_TEXT_AREA))
                .contains(caseData.getClaimantRequests().getOtherClaim());
        }
    }

}
