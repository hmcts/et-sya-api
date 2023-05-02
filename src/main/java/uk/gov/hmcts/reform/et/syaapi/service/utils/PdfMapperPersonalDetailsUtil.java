package uk.gov.hmcts.reform.et.syaapi.service.utils;

import org.apache.commons.lang3.StringUtils;
import org.springframework.util.ObjectUtils;
import uk.gov.hmcts.et.common.model.ccd.Address;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.types.ClaimantIndType;
import uk.gov.hmcts.et.common.model.ccd.types.ClaimantType;

import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;
import static uk.gov.hmcts.reform.et.syaapi.service.pdf.PdfMapperService.EMAIL;
import static uk.gov.hmcts.reform.et.syaapi.service.pdf.PdfMapperService.POST;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.PdfMapperConstants.OTHER;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.PdfMapperConstants.OTHER_SPECIFY;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.PdfMapperConstants.PHONE_NUMBER_PREFIX;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.PdfMapperConstants.SEX_FEMALE;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.PdfMapperConstants.SEX_FEMALE_LOWERCASE;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.PdfMapperConstants.SEX_MALE;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.PdfMapperConstants.SEX_PREFER_NOT_TO_SAY;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.PdfMapperConstants.SEX_PREFER_NOT_TO_SAY_LOWERCASE;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.PdfMapperConstants.TITLE_MAP;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.PdfMapperConstants.TITLES;

/**
 * Mapper for personal details on the case data to the ET1 Pdf form.
 */
public final class PdfMapperPersonalDetailsUtil {

    private PdfMapperPersonalDetailsUtil() {
        // Final class
    }

    public static void putPersonalDetails(CaseData caseData, ConcurrentMap<String, Optional<String>> printFields) {
        try {
            printFields.putAll(mapClaimantIndType(caseData.getClaimantIndType()));
            printFields.putAll(mapClaimantType(caseData.getClaimantType()));
        } catch (Exception e) {
            GenericServiceUtil.logException("An error occured while mapping personal details",
                                            ObjectUtils.isEmpty(caseData) ? "" : caseData.getEthosCaseReference(),
                                            e.getMessage(),
                                            "PdfMapperPersonalDetailsUtil", "mapPersonalDetials");
        }
    }

    private static Map<String, Optional<String>> mapClaimantIndType(ClaimantIndType claimantIndType) {
        Map<String, Optional<String>> printFields = new ConcurrentHashMap<>();
        if (ObjectUtils.isEmpty(claimantIndType)) {
            return printFields;
        }

        String claimantPreferredTitle = claimantIndType.getClaimantPreferredTitle();
        if (StringUtils.isNotBlank(claimantPreferredTitle) && TITLES.containsKey(claimantPreferredTitle)) {
            printFields.put(TITLES.get(claimantPreferredTitle), ofNullable(TITLE_MAP.get(claimantPreferredTitle)));
            if (OTHER.equals(claimantPreferredTitle)) {
                printFields.put(
                    TITLES.get(OTHER_SPECIFY),
                    ofNullable(String.valueOf(claimantIndType.getClaimantTitleOther()))
                );
            }
        }
        printFields.put(PdfMapperConstants.Q1_FIRST_NAME, ofNullable(claimantIndType.getClaimantFirstNames()));
        printFields.put(PdfMapperConstants.Q1_SURNAME, ofNullable(claimantIndType.getClaimantLastName()));
        printFields.putAll(mapDobFields(claimantIndType.getClaimantDateOfBirth()));
        printFields.putAll(mapSexFields(claimantIndType.getClaimantSex()));
        return printFields;
    }

    private static Map<String, Optional<String>> mapDobFields(String claimantDateOfBirth) {
        Map<String, Optional<String>> dobFields = new ConcurrentHashMap<>();

        if (StringUtils.isBlank(claimantDateOfBirth)) {
            return dobFields;
        }

        LocalDate dob = LocalDate.parse(claimantDateOfBirth);

        dobFields.put(
            PdfMapperConstants.Q1_DOB_DAY,
            Optional.of(StringUtils.leftPad(
                String.valueOf(dob.getDayOfMonth()),
                2,
                "0"
            ))
        );

        dobFields.put(
            PdfMapperConstants.Q1_DOB_MONTH,
            Optional.of(StringUtils.leftPad(
                String.valueOf(dob.getMonthValue()),
                2,
                "0"
            ))
        );
        dobFields.put(PdfMapperConstants.Q1_DOB_YEAR, Optional.of(String.valueOf(dob.getYear())));

        return dobFields;
    }

    private static Map<String, Optional<String>> mapSexFields(String claimantSex) {
        Map<String, Optional<String>> sexFields = new ConcurrentHashMap<>();

        if (StringUtils.isNotBlank(claimantSex)) {
            switch (claimantSex) {
                case SEX_MALE -> sexFields.put(PdfMapperConstants.Q1_SEX_MALE, Optional.of(YES));
                case SEX_FEMALE -> sexFields.put(PdfMapperConstants.Q1_SEX_FEMALE, Optional.of(SEX_FEMALE_LOWERCASE));
                case SEX_PREFER_NOT_TO_SAY ->
                    sexFields.put(PdfMapperConstants.Q1_SEX_PREFER_NOT_TO_SAY, Optional.of(SEX_PREFER_NOT_TO_SAY_LOWERCASE));
                default -> throw new IllegalStateException("Can't have this as the claimant's sex: " + claimantSex);
            }
        }
        return sexFields;
    }

    private static Map<String, Optional<String>> mapClaimantType(ClaimantType claimantType) {
        Map<String, Optional<String>> printFields = new ConcurrentHashMap<>();
        if (ObjectUtils.isEmpty(claimantType)) {
            return printFields;
        }

        Address claimantAddressUK = claimantType.getClaimantAddressUK();
        if (claimantAddressUK != null) {
            printFields.put(PdfMapperConstants.Q1_6_CLAIMANT_ADDRESS,
                            ofNullable(PdfMapperServiceUtil.formatAddressForTextField(claimantAddressUK)));
            printFields.put(PdfMapperConstants.Q1_6_CLAIMANT_POSTCODE,
                            ofNullable(PdfMapperServiceUtil.formatUkPostcode(claimantAddressUK)));
        }

        printFields.put(
            String.format(PdfMapperConstants.QX_PHONE_NUMBER, PHONE_NUMBER_PREFIX),
            ofNullable(claimantType.getClaimantPhoneNumber())
        );

        printFields.put(
            PdfMapperConstants.Q1_MOBILE_NUMBER,
            ofNullable(claimantType.getClaimantMobileNumber())
        );

        printFields.put(
            PdfMapperConstants.Q1_EMAIL,
            ofNullable(claimantType.getClaimantEmailAddress())
        );

        String contactPreference = claimantType.getClaimantContactPreference();

        if (EMAIL.equals(contactPreference)) {
            printFields.put(PdfMapperConstants.Q1_CONTACT_EMAIL, Optional.of(contactPreference));
        } else if (POST.equals(contactPreference)) {
            printFields.put(PdfMapperConstants.Q1_CONTACT_POST, Optional.of(contactPreference));
        }

        return printFields;
    }
}
