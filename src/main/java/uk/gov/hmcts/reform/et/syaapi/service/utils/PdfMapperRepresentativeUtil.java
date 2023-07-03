package uk.gov.hmcts.reform.et.syaapi.service.utils;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import uk.gov.hmcts.et.common.model.ccd.Address;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.types.RepresentedTypeC;

import java.util.Optional;
import java.util.concurrent.ConcurrentMap;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.PdfMapperConstants.EMAIL;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.PdfMapperConstants.FAX;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.PdfMapperConstants.POST;

public final class PdfMapperRepresentativeUtil {

    private PdfMapperRepresentativeUtil() {
        // Utility classes should not have a public or default constructor.
    }

    public static void putRepresentative(CaseData caseData, ConcurrentMap<String, Optional<String>> printFields) {
        try {
            if (ObjectUtils.isNotEmpty(caseData.getRepresentativeClaimantType())) {
                putGenericRepresentativeFields(caseData.getRepresentativeClaimantType(), printFields);
                putRepresentativeAddress(caseData.getRepresentativeClaimantType().getRepresentativeAddress(),
                                         printFields);
                putRepresentativeCommunicationPreference(
                    caseData.getRepresentativeClaimantType().getRepresentativePreference(), printFields);
            }
        } catch (Exception e) {
            GenericServiceUtil.logException("An error occured while printing representative details to pdf file",
                                            caseData.getEthosCaseReference(),
                                            e.getMessage(),
                                            "PdfMapperRepresentativeUtil",
                                            "putRepresentative");
        }
    }

    private static void putGenericRepresentativeFields(RepresentedTypeC representativeClaimantType,
                                                       ConcurrentMap<String, Optional<String>> printFields) {
        printFields.put(
            PdfMapperConstants.Q11_REP_NAME,
            ofNullable(representativeClaimantType.getNameOfRepresentative())
        );
        printFields.put(
            PdfMapperConstants.Q11_REP_ORG,
            ofNullable(representativeClaimantType.getNameOfOrganisation())
        );
        printFields.put(PdfMapperConstants.Q11_REP_NUMBER, Optional.of(""));
        printFields.put(
            PdfMapperConstants.Q11_PHONE_NUMBER,
            ofNullable(representativeClaimantType.getRepresentativePhoneNumber())
        );
        printFields.put(
            PdfMapperConstants.Q11_MOBILE_NUMBER,
            ofNullable(representativeClaimantType.getRepresentativeMobileNumber())
        );
        printFields.put(
            PdfMapperConstants.Q11_EMAIL,
            ofNullable(representativeClaimantType.getRepresentativeEmailAddress())
        );
        printFields.put(
            PdfMapperConstants.Q11_REFERENCE,
            ofNullable(representativeClaimantType.getRepresentativeReference())
        );

    }

    private static void putRepresentativeAddress(Address representativeAddress,
                                                 ConcurrentMap<String, Optional<String>> printFields) {
        if (ObjectUtils.isNotEmpty(representativeAddress)) {
            printFields.put(
                PdfMapperConstants.Q11_3_REPRESENTATIVE_ADDRESS,
                ofNullable(PdfMapperServiceUtil.formatAddressForTextField(representativeAddress))
            );
            printFields.put(
                PdfMapperConstants.Q11_3_REPRESENTATIVE_POSTCODE,
                ofNullable(PdfMapperServiceUtil.formatUkPostcode(representativeAddress))
            );
        }
    }

    private static void putRepresentativeCommunicationPreference(String representativeCommunicationPreference,
                                                                 ConcurrentMap<String, Optional<String>> printFields) {
        if (StringUtils.isNotBlank(representativeCommunicationPreference)) {
            if (EMAIL.equals(representativeCommunicationPreference)) {
                printFields.put(PdfMapperConstants.Q11_CONTACT_EMAIL, Optional.of(EMAIL));
            } else if (POST.equals(representativeCommunicationPreference)) {
                printFields.put(PdfMapperConstants.Q11_CONTACT_POST, Optional.of(POST));
            } else {
                printFields.put(PdfMapperConstants.Q11_CONTACT_POST, Optional.of(FAX));
            }
        }
    }
}
