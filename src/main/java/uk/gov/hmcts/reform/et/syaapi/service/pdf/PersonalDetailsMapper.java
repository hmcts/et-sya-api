package uk.gov.hmcts.reform.et.syaapi.service.pdf;

import org.apache.commons.lang3.StringUtils;
import uk.gov.hmcts.et.common.model.ccd.Address;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.types.ClaimantIndType;
import uk.gov.hmcts.et.common.model.ccd.types.ClaimantType;

import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.et.syaapi.service.pdf.PdfMapperService.EMAIL;
import static uk.gov.hmcts.reform.et.syaapi.service.pdf.PdfMapperService.POST;

/**
 * Mapper for personal details on the case data to the ET1 Pdf form.
 */
public class PersonalDetailsMapper {
    private static final String OTHER = "Other";

    private static final Map<String, String> TITLES = Map.of(
        "Mr", PdfMapperConstants.Q1_TITLE_MR,
        "Mrs", PdfMapperConstants.Q1_TITLE_MRS,
        "Miss", PdfMapperConstants.Q1_TITLE_MISS,
        "Ms", PdfMapperConstants.Q1_TITLE_MS,
        OTHER, PdfMapperConstants.Q1_TITLE_OTHER,
        "Other_Specify", PdfMapperConstants.Q1_TITLE_OTHER_SPECIFY

    );
    private static final Map<String, String> TITLE_MAP = Map.of(
        "Mr", "Mister",
        "Mrs", "Missus",
        "Miss", "Miss",
        "Ms", "Miz",
        OTHER, "Miz"
    );

    private static final String PHONE_NUMBER_PREFIX = "1.6";

    public Map<String, Optional<String>> mapPersonalDetails(CaseData caseData) {
        Map<String, Optional<String>> printFields = new ConcurrentHashMap<>();

        printFields.putAll(mapClaimantIndType(caseData.getClaimantIndType()));
        printFields.putAll(mapClaimantType(caseData.getClaimantType()));

        return printFields;
    }

    private Map<String, Optional<String>> mapClaimantIndType(ClaimantIndType claimantIndType) {
        Map<String, Optional<String>> printFields = new ConcurrentHashMap<>();
        if (claimantIndType == null) {
            return printFields;
        }

        String claimantPreferredTitle = claimantIndType.getClaimantPreferredTitle();
        if (claimantPreferredTitle != null && TITLES.containsKey(claimantPreferredTitle)) {
            printFields.put(TITLES.get(claimantPreferredTitle), ofNullable(TITLE_MAP.get(claimantPreferredTitle)));
            if (OTHER.equals(claimantPreferredTitle)) {
                printFields.put(
                    TITLES.get("Other_Specify"),
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

    private Map<String, Optional<String>> mapDobFields(String claimantDateOfBirth) {
        Map<String, Optional<String>> dobFields = new ConcurrentHashMap<>();

        if (claimantDateOfBirth == null) {
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

    private Map<String, Optional<String>> mapSexFields(String claimantSex) {
        Map<String, Optional<String>> sexFields = new ConcurrentHashMap<>();

        if (claimantSex != null) {
            switch (claimantSex) {
                case "Male":
                    sexFields.put(PdfMapperConstants.Q1_SEX_MALE, Optional.of("Yes"));
                    break;
                case "Female":
                    sexFields.put(PdfMapperConstants.Q1_SEX_FEMALE, Optional.of("female"));
                    break;
                case "Prefer not to say":
                    sexFields.put(PdfMapperConstants.Q1_SEX_PREFER_NOT_TO_SAY, Optional.of("prefer not to say"));
                    break;
                default:
                    throw new IllegalStateException("Can't have this as the claimant's sex: " + claimantSex);
            }
        }
        return sexFields;
    }

    private Map<String, Optional<String>> mapClaimantType(ClaimantType claimantType) {
        Map<String, Optional<String>> printFields = new ConcurrentHashMap<>();
        if (claimantType == null) {
            return printFields;
        }

        Address claimantAddressUK = claimantType.getClaimantAddressUK();
        if (claimantAddressUK != null) {
            printFields.put(PdfMapperConstants.Q1_6_CLAIMANT_ADDRESS,
                            ofNullable(PdfMapperUtil.formatAddressForTextField(claimantAddressUK)));
            printFields.put(PdfMapperConstants.Q1_6_CLAIMANT_POSTCODE,
                            ofNullable(PdfMapperUtil.formatUkPostcode(claimantAddressUK)));
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
