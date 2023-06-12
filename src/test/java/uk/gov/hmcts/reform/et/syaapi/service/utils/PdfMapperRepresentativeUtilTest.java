package uk.gov.hmcts.reform.et.syaapi.service.utils;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.types.RepresentedTypeC;
import uk.gov.hmcts.reform.et.syaapi.model.PdfMapperTestData;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.PdfMapperConstants.EMAIL;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.PdfMapperConstants.FAX;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.PdfMapperConstants.POST;

class PdfMapperRepresentativeUtilTest {

    private CaseData caseData;

    @BeforeEach
    void beforeEach() {
        caseData = new CaseData();
        caseData.setEthosCaseReference("1234567890");
    }

    @ParameterizedTest
    @NullSource
    @MethodSource("uk.gov.hmcts.reform.et.syaapi.model.PdfMapperTestData#generateRepresentativeClaimantTypes")
    void putRepresentative(RepresentedTypeC representativeClaimantType) {
        ConcurrentMap<String, Optional<String>> printFields = new ConcurrentHashMap<>();
        caseData.setRepresentativeClaimantType(representativeClaimantType);
        PdfMapperRepresentativeUtil.putRepresentative(caseData, printFields);
        if (ObjectUtils.isNotEmpty(representativeClaimantType)) {
            assertThat(printFields.get(PdfMapperConstants.Q11_REP_NAME))
                .contains(representativeClaimantType.getNameOfRepresentative());
            assertThat(printFields.get(PdfMapperConstants.Q11_REP_ORG))
                .contains(representativeClaimantType.getNameOfOrganisation());
            assertThat(printFields.get(PdfMapperConstants.Q11_PHONE_NUMBER))
                .contains(representativeClaimantType.getRepresentativePhoneNumber());
            assertThat(printFields.get(PdfMapperConstants.Q11_MOBILE_NUMBER))
                .contains(representativeClaimantType.getRepresentativeMobileNumber());
            assertThat(printFields.get(PdfMapperConstants.Q11_EMAIL))
                .contains(representativeClaimantType.getRepresentativeEmailAddress());
            assertThat(printFields.get(PdfMapperConstants.Q11_REFERENCE))
                .contains(representativeClaimantType.getRepresentativeReference());
            if (ObjectUtils.isNotEmpty(caseData.getRepresentativeClaimantType().getRepresentativeAddress())) {
                assertThat(printFields.get(PdfMapperConstants.Q11_3_REPRESENTATIVE_ADDRESS))
                    .contains(PdfMapperServiceUtil.formatAddressForTextField(
                        caseData.getRepresentativeClaimantType().getRepresentativeAddress()));
                assertThat(printFields.get(PdfMapperConstants.Q11_3_REPRESENTATIVE_POSTCODE))
                    .contains(PdfMapperServiceUtil.formatUkPostcode(
                        caseData.getRepresentativeClaimantType().getRepresentativeAddress()));
            } else {
                assertThat(printFields.get(PdfMapperConstants.Q11_3_REPRESENTATIVE_ADDRESS)).isNull();
                assertThat(printFields.get(PdfMapperConstants.Q11_3_REPRESENTATIVE_POSTCODE)).isNull();
            }
            if (StringUtils.isNotBlank(caseData.getRepresentativeClaimantType().getRepresentativePreference())) {
                if (EMAIL.equals(caseData.getRepresentativeClaimantType().getRepresentativePreference())) {
                    assertThat(printFields.get(PdfMapperConstants.Q11_CONTACT_EMAIL))
                        .contains(EMAIL);
                } else if (POST.equals(caseData.getRepresentativeClaimantType().getRepresentativePreference())) {
                    assertThat(printFields.get(PdfMapperConstants.Q11_CONTACT_POST))
                        .contains(POST);
                } else {
                    assertThat(printFields.get(PdfMapperConstants.Q11_CONTACT_POST))
                        .contains(FAX);
                }
            } else {
                assertThat(printFields.get(PdfMapperConstants.Q11_CONTACT_POST)).isNull();
            }
        } else {
            assertThat(printFields.get(PdfMapperConstants.Q11_REP_NAME)).isNull();
            assertThat(printFields.get(PdfMapperConstants.Q11_REP_ORG)).isNull();
            assertThat(printFields.get(PdfMapperConstants.Q11_PHONE_NUMBER)).isNull();
            assertThat(printFields.get(PdfMapperConstants.Q11_MOBILE_NUMBER)).isNull();
            assertThat(printFields.get(PdfMapperConstants.Q11_EMAIL)).isNull();
            assertThat(printFields.get(PdfMapperConstants.Q11_REFERENCE)).isNull();
            assertThat(printFields.get(PdfMapperConstants.Q11_3_REPRESENTATIVE_ADDRESS)).isNull();
            assertThat(printFields.get(PdfMapperConstants.Q11_3_REPRESENTATIVE_POSTCODE)).isNull();
            assertThat(printFields.get(PdfMapperConstants.Q11_CONTACT_EMAIL)).isNull();
        }
    }
}
