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
import static uk.gov.hmcts.reform.et.syaapi.service.pdf.PdfMapperService.formatPostcode;

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

    private static final String CLAIMANT_ADDRESS_PREFIX = "1.5";

    // todo split further and add tests.
    public Map<String, Optional<String>> printPersonalDetails(CaseData caseData) {
        ConcurrentHashMap<String, Optional<String>> printFields = new ConcurrentHashMap<>();

        ClaimantIndType claimantIndType = caseData.getClaimantIndType();
        if (claimantIndType != null) {
            if (claimantIndType.getClaimantPreferredTitle() != null
                && TITLES.containsKey(claimantIndType.getClaimantPreferredTitle())) {
                printFields.put(
                    TITLES.get(claimantIndType.getClaimantPreferredTitle()),
                    ofNullable(TITLE_MAP.get(claimantIndType.getClaimantPreferredTitle()))
                );
                if (OTHER.equals(claimantIndType.getClaimantPreferredTitle())) {
                    printFields.put(
                        TITLES.get("Other_Specify"),
                        ofNullable(String.valueOf(claimantIndType.getClaimantTitleOther()))
                    );
                }
            }

            printFields.put(
                PdfMapperConstants.Q1_FIRST_NAME,
                ofNullable(claimantIndType.getClaimantFirstNames())
            );

            printFields.put(
                PdfMapperConstants.Q1_SURNAME,
                ofNullable(claimantIndType.getClaimantLastName())
            );

            printFields.putAll(getDobPrintFields(claimantIndType.getClaimantDateOfBirth()));

            if (claimantIndType.getClaimantSex() != null) {
                if ("Male".equals(claimantIndType.getClaimantSex())) {
                    printFields.put(PdfMapperConstants.Q1_SEX_MALE, Optional.of("Yes"));
                } else if ("Female".equals(claimantIndType.getClaimantSex())) {
                    printFields.put(PdfMapperConstants.Q1_SEX_FEMALE, Optional.of("female"));
                } else if ("Prefer not to say".equals(claimantIndType.getClaimantSex())) {
                    printFields.put(PdfMapperConstants.Q1_SEX_PREFER_NOT_TO_SAY, Optional.of("prefer not to say"));
                }
            }
        }

        ClaimantType claimantType = caseData.getClaimantType();
        if (claimantType != null) {
            Address claimantAddressUK = claimantType.getClaimantAddressUK();
            if (claimantAddressUK != null) {
                printFields.put(
                    String.format(PdfMapperConstants.RP2_HOUSE_NUMBER, CLAIMANT_ADDRESS_PREFIX),
                    ofNullable(claimantAddressUK.getAddressLine1())
                );
                printFields.put(
                    String.format(PdfMapperConstants.QX_STREET, CLAIMANT_ADDRESS_PREFIX),
                    ofNullable(claimantAddressUK.getAddressLine2())
                );
                printFields.put(
                    String.format(PdfMapperConstants.RP_POST_TOWN, CLAIMANT_ADDRESS_PREFIX),
                    ofNullable(claimantAddressUK.getPostTown())
                );
                printFields.put(
                    String.format(PdfMapperConstants.QX_COUNTY, CLAIMANT_ADDRESS_PREFIX),
                    ofNullable(claimantAddressUK.getCounty())
                );
                printFields.put(
                    String.format(PdfMapperConstants.QX_POSTCODE, CLAIMANT_ADDRESS_PREFIX),
                    ofNullable(formatPostcode(claimantAddressUK.getPostCode()))
                );
            }

            printFields.put(
                String.format(PdfMapperConstants.QX_PHONE_NUMBER, "1.6"),
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
            } else if ("Post".equals(contactPreference)) {
                printFields.put(PdfMapperConstants.Q1_CONTACT_POST, Optional.of(contactPreference));
            }
        }
        return printFields;
    }

    private Map<String, Optional<String>> getDobPrintFields(String claimantDateOfBirth) {
        ConcurrentHashMap<String, Optional<String>> dobFields = new ConcurrentHashMap<>();

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
}
