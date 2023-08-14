package uk.gov.hmcts.reform.et.syaapi.service.utils.data;

import org.junit.jupiter.params.provider.Arguments;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.types.ClaimantRequestType;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static uk.gov.hmcts.reform.et.syaapi.constants.ClaimTypesConstants.AGE;
import static uk.gov.hmcts.reform.et.syaapi.constants.ClaimTypesConstants.ARREARS;
import static uk.gov.hmcts.reform.et.syaapi.constants.ClaimTypesConstants.BREACH_OF_CONTRACT;
import static uk.gov.hmcts.reform.et.syaapi.constants.ClaimTypesConstants.DISABILITY;
import static uk.gov.hmcts.reform.et.syaapi.constants.ClaimTypesConstants.DISCRIMINATION;
import static uk.gov.hmcts.reform.et.syaapi.constants.ClaimTypesConstants.ETHNICITY;
import static uk.gov.hmcts.reform.et.syaapi.constants.ClaimTypesConstants.GENDER_REASSIGNMENT;
import static uk.gov.hmcts.reform.et.syaapi.constants.ClaimTypesConstants.HOLIDAY_PAY;
import static uk.gov.hmcts.reform.et.syaapi.constants.ClaimTypesConstants.MARRIAGE_OR_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.reform.et.syaapi.constants.ClaimTypesConstants.NOTICE_PAY;
import static uk.gov.hmcts.reform.et.syaapi.constants.ClaimTypesConstants.OTHER_PAYMENTS;
import static uk.gov.hmcts.reform.et.syaapi.constants.ClaimTypesConstants.OTHER_TYPES;
import static uk.gov.hmcts.reform.et.syaapi.constants.ClaimTypesConstants.PAY_RELATED_CLAIM;
import static uk.gov.hmcts.reform.et.syaapi.constants.ClaimTypesConstants.PREGNANCY_OR_MATERNITY;
import static uk.gov.hmcts.reform.et.syaapi.constants.ClaimTypesConstants.RACE;
import static uk.gov.hmcts.reform.et.syaapi.constants.ClaimTypesConstants.REDUNDANCY_PAY;
import static uk.gov.hmcts.reform.et.syaapi.constants.ClaimTypesConstants.RELIGION_OR_BELIEF;
import static uk.gov.hmcts.reform.et.syaapi.constants.ClaimTypesConstants.SEX;
import static uk.gov.hmcts.reform.et.syaapi.constants.ClaimTypesConstants.SEXUAL_ORIENTATION;
import static uk.gov.hmcts.reform.et.syaapi.constants.ClaimTypesConstants.UNFAIR_DISMISSAL;
import static uk.gov.hmcts.reform.et.syaapi.constants.ClaimTypesConstants.WHISTLE_BLOWING;

public final class PdfMapperClaimDetailsUtilTestDataProvider {

    private PdfMapperClaimDetailsUtilTestDataProvider() {
        // Utility classes should not have a public or default constructor.
    }

    public static Stream<Arguments> generateCaseDataSamplesForClaimDetailsUtilTest() {
        ////// CASE DATA 1 --> All discrimination, all payment except redundancy pay
        CaseData caseData1 = new CaseData();
        caseData1.setTypesOfClaim(Arrays.asList(BREACH_OF_CONTRACT, DISCRIMINATION, PAY_RELATED_CLAIM,
                                                UNFAIR_DISMISSAL, WHISTLE_BLOWING, OTHER_TYPES));
        ClaimantRequestType claimantRequestType = new ClaimantRequestType();
        claimantRequestType.setPayClaims(Arrays.asList(ARREARS, HOLIDAY_PAY, NOTICE_PAY, OTHER_PAYMENTS));
        claimantRequestType.setDiscriminationClaims(Arrays.asList(AGE, DISABILITY, ETHNICITY, GENDER_REASSIGNMENT,
                                                                  MARRIAGE_OR_CIVIL_PARTNERSHIP,
                                                                  PREGNANCY_OR_MATERNITY,
                                                                  RACE,
                                                                  RELIGION_OR_BELIEF,
                                                                  SEX,
                                                                  SEXUAL_ORIENTATION));
        caseData1.setClaimantRequests(claimantRequestType);
        ////// CASE DATA 3 --> Other types text area filled
        CaseData caseData3 = new CaseData();
        caseData3.setTypesOfClaim(List.of(OTHER_TYPES));
        claimantRequestType.setOtherClaim("Test Other Claim");
        ////// CASE DATA 4 --> Redundancy Pay selected
        CaseData caseData4 = new CaseData();
        caseData4.setTypesOfClaim(List.of(PAY_RELATED_CLAIM));
        ClaimantRequestType claimantRequestType2 = new ClaimantRequestType();
        claimantRequestType2.setPayClaims(List.of(REDUNDANCY_PAY));
        caseData4.setClaimantRequests(claimantRequestType2);
        ////// CASE DATA 2 --> Case Data not null but all others are null
        CaseData caseData2 = new CaseData();
        return Stream.of(Arguments.of(caseData1),
                         Arguments.of(caseData2),
                         Arguments.of(caseData3),
                         Arguments.of(caseData4));
    }

}
