package uk.gov.hmcts.reform.et.syaapi.service.pdf.util;

import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.common.Strings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import uk.gov.hmcts.et.common.model.ccd.Address;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.RespondentSumType;
import uk.gov.hmcts.reform.et.syaapi.models.AcasCertificatePdfFieldModel;
import uk.gov.hmcts.reform.et.syaapi.models.RespondentPdfFieldModel;
import uk.gov.hmcts.reform.et.syaapi.service.utils.GenericServiceUtil;
import uk.gov.hmcts.reform.et.syaapi.service.utils.PdfMapperConstants;
import uk.gov.hmcts.reform.et.syaapi.service.utils.PdfMapperRespondentUtil;
import uk.gov.hmcts.reform.et.syaapi.service.utils.PdfMapperServiceUtil;
import uk.gov.hmcts.reform.et.syaapi.service.utils.PdfTemplateRespondentFieldNamesEnum;
import uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants;
import uk.gov.hmcts.reform.et.syaapi.service.utils.data.PdfMapperRespondentUtilTestDataProvider;
import uk.gov.hmcts.reform.et.syaapi.service.utils.data.TestDataProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.PdfMapperConstants.NO_LOWERCASE;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.PdfMapperConstants.PDF_TEMPLATE_Q13_2_1_2_FORTH_RESPONDENT_ACAS_CERTIFICATE_CHECK_NO;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.PdfMapperConstants.PDF_TEMPLATE_Q2_3_1_2_FIRST_RESPONDENT_ACAS_CERTIFICATE_CHECK_NO;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.PdfMapperConstants.PDF_TEMPLATE_Q2_8_1_2_THIRD_RESPONDENT_ACAS_CERTIFICATE_CHECK_NO;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.NO;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.YES;

class PdfMapperRespondentUtilTest {

    @ParameterizedTest
    @NullSource
    @MethodSource(
        "uk.gov.hmcts.reform.et.syaapi.service.utils.data.PdfMapperRespondentUtilTestDataProvider#"
            + "generateCaseDataSamplesWithRespondentSumTypeItems")
    void putRespondent(CaseData respondentCaseData) {
        ConcurrentMap<String, Optional<String>> printFields = new ConcurrentHashMap<>();
        PdfMapperRespondentUtil.putRespondents(respondentCaseData, printFields);
        if (ObjectUtils.isEmpty(respondentCaseData)
            || CollectionUtils.isEmpty(respondentCaseData.getRespondentCollection())) {
            checkClaimantWorkAddress(printFields, respondentCaseData);
        } else {
            RespondentPdfFieldModel firstRespondentPdfFieldModel =
                PdfTemplateRespondentFieldNamesEnum.FIRST_RESPONDENT.respondentPdfFieldModel;
            RespondentSumType firstRespondent = respondentCaseData.getRespondentCollection().get(0).getValue();
            checkClaimantWorkAddress(printFields, respondentCaseData);
            checkRespondentNameAddress(printFields, firstRespondentPdfFieldModel, firstRespondent);
            checkAcasCertificate(printFields,
                                 firstRespondent,
                                 firstRespondentPdfFieldModel.respondentAcasCertificatePdfFieldModel());
            if (respondentCaseData.getRespondentCollection().size() >= TestConstants.INTEGER_NUMERIC_TWO) {
                RespondentPdfFieldModel secondRespondentPdfFieldModel =
                    PdfTemplateRespondentFieldNamesEnum.SECOND_RESPONDENT.respondentPdfFieldModel;
                RespondentSumType secondRespondent = respondentCaseData.getRespondentCollection().get(1).getValue();
                checkRespondentNameAddress(printFields, secondRespondentPdfFieldModel, secondRespondent);
                checkAcasCertificate(printFields,
                                     secondRespondent,
                                     secondRespondentPdfFieldModel.respondentAcasCertificatePdfFieldModel());
            }
            if (respondentCaseData.getRespondentCollection().size() >= TestConstants.INTEGER_NUMERIC_THREE) {
                RespondentPdfFieldModel thirdRespondentPdfFieldModel =
                    PdfTemplateRespondentFieldNamesEnum.THIRD_RESPONDENT.respondentPdfFieldModel;
                RespondentSumType thirdRespondent = respondentCaseData.getRespondentCollection().get(2).getValue();
                checkRespondentNameAddress(printFields, thirdRespondentPdfFieldModel, thirdRespondent);
                checkAcasCertificate(printFields,
                                     thirdRespondent,
                                     thirdRespondentPdfFieldModel.respondentAcasCertificatePdfFieldModel());
            }
            if (respondentCaseData.getRespondentCollection().size() >= TestConstants.INTEGER_NUMERIC_FOUR) {
                RespondentPdfFieldModel forthRespondentPdfFieldModel =
                    PdfTemplateRespondentFieldNamesEnum.FORTH_RESPONDENT.respondentPdfFieldModel;
                RespondentSumType forthRespondent = respondentCaseData.getRespondentCollection().get(3).getValue();
                checkRespondentNameAddress(printFields, forthRespondentPdfFieldModel, forthRespondent);
                checkAcasCertificate(printFields,
                                     forthRespondent,
                                     forthRespondentPdfFieldModel.respondentAcasCertificatePdfFieldModel());
            }
            if (respondentCaseData.getRespondentCollection().size() >= TestConstants.INTEGER_NUMERIC_FIVE) {
                RespondentPdfFieldModel fifthRespondentPdfFieldModel =
                    PdfTemplateRespondentFieldNamesEnum.FIFTH_RESPONDENT.respondentPdfFieldModel;
                RespondentSumType fifthRespondent = respondentCaseData.getRespondentCollection().get(4).getValue();
                checkRespondentNameAddress(printFields, fifthRespondentPdfFieldModel, fifthRespondent);
                checkAcasCertificate(printFields,
                                     fifthRespondent,
                                     fifthRespondentPdfFieldModel.respondentAcasCertificatePdfFieldModel());
            }
        }
    }

    @Test
    void putRespondentLogsPdfServiceExceptionWhenWrongNoAcasReasonSelected() {
        try (MockedStatic<GenericServiceUtil> mockedServiceUtil = Mockito.mockStatic(GenericServiceUtil.class)) {
            Address respondentAddress = TestDataProvider.generateAddressByAddressFields(TestConstants.ADDRESS_LINE_1,
                                                                                        TestConstants.ADDRESS_LINE_2,
                                                                                        TestConstants.ADDRESS_LINE_3,
                                                                                        TestConstants.POST_TOWN,
                                                                                        TestConstants.COUNTY,
                                                                                        TestConstants.COUNTRY,
                                                                                        TestConstants.POSTCODE);
            CaseData respondentCaseData =
                TestDataProvider.generateCaseDataForRespondent(TestConstants.STRING_NUMERIC_ONE,
                                                               YES,
                                                               TestConstants.NULL_ADDRESS);
            RespondentSumTypeItem respondentSumTypeItem =
                PdfMapperRespondentUtilTestDataProvider.generateRespondentSumTypeItem(TestConstants.STRING_NUMERIC_ONE,
                                                                                      TestConstants.TEST_COMPANY_NAME,
                                                                                      respondentAddress,
                                                                                      NO,
                                                                                      TestConstants.NULL_STRING,
                                                                                      "DUMMY REASON");

            List<RespondentSumTypeItem> respondentCollection = new ArrayList<>();
            respondentCollection.add(respondentSumTypeItem);
            respondentCaseData.setRespondentCollection(respondentCollection);
            ConcurrentMap<String, Optional<String>> printFields = new ConcurrentHashMap<>();
            PdfMapperRespondentUtil.putRespondents(respondentCaseData, printFields);
            mockedServiceUtil.verify(
                () -> GenericServiceUtil.logException(anyString(), anyString(), anyString(), anyString(), anyString()),
                times(1)
            );
        }
    }

    private static void checkClaimantWorkAddress(ConcurrentMap<String,
        Optional<String>> printFields, CaseData caseData) {
        if (!ObjectUtils.isEmpty(caseData)
            && NO.equals(caseData.getClaimantWorkAddressQuestion())
            && !ObjectUtils.isEmpty(caseData.getClaimantWorkAddress())) {
            checkAddress(printFields, caseData.getClaimantWorkAddress().getClaimantWorkAddress(),
                         PdfMapperConstants.PDF_TEMPLATE_Q2_4_1_CLAIMANT_WORK_ADDRESS,
                         PdfMapperConstants.PDF_TEMPLATE_Q2_4_2_CLAIMANT_WORK_POSTCODE);
        }
    }

    private static void checkRespondentNameAddress(ConcurrentMap<String, Optional<String>> printFields,
                                                   RespondentPdfFieldModel respondentPdfFieldModel,
                                                   RespondentSumType respondent) {
        assertThat(printFields.get(respondentPdfFieldModel.respondentNameFieldName()))
            .contains(respondent.getRespondentName());
        checkAddress(printFields,
                     respondent.getRespondentAddress(),
                     respondentPdfFieldModel.respondentAddressFieldName(),
                     respondentPdfFieldModel.respondentPostcodeFieldName());
    }

    private static void checkAddress(ConcurrentMap<String, Optional<String>> printFields,
                                     Address address,
                                     String addressField,
                                     String postCodeField
                                     ) {
        assertThat(printFields.get(addressField))
            .contains(PdfMapperServiceUtil.formatAddressForTextField(address));
        assertThat(printFields.get(postCodeField))
            .contains(PdfMapperServiceUtil.formatUkPostcode(address));
    }

    private static void checkAcasCertificate(ConcurrentMap<String, Optional<String>> printFields,
                                             RespondentSumType respondent,
                                             AcasCertificatePdfFieldModel acasCertificatePdfFieldModel) {
        if (StringUtils.isNotBlank(respondent.getRespondentAcasQuestion())
            && YES.equals(respondent.getRespondentAcasQuestion())) {
            assertThat(printFields.get(acasCertificatePdfFieldModel.getAcasCertificateCheckYesFieldName()))
                .contains(YES);
            assertThat(printFields.get(acasCertificatePdfFieldModel.getAcasCertificateNumberFieldName()))
                .contains(respondent.getRespondentAcas());
        } else {
            if (!Strings.isNullOrEmpty(respondent.getRespondentAcasNo())) {
                checkAcasCertificateNotFoundCheckbox(acasCertificatePdfFieldModel, printFields);
                switch (respondent.getRespondentAcasNo()) {
                    case PdfMapperConstants.PDF_TEMPLATE_REASON_NOT_HAVING_ACAS_UNFAIR_DISMISSAL: {
                        assertThat(printFields.get(
                            acasCertificatePdfFieldModel.getNoAcasReasonUnfairDismissalFieldName()))
                            .contains(YES);
                        break;
                    }
                    case PdfMapperConstants.PDF_TEMPLATE_REASON_NOT_HAVING_ACAS_ANOTHER_PERSON: {
                        assertThat(printFields.get(
                            acasCertificatePdfFieldModel.getNoAcasReasonAnotherPersonFieldName()))
                            .contains(YES);
                        break;
                    }
                    case PdfMapperConstants.PDF_TEMPLATE_REASON_NOT_HAVING_ACAS_NO_POWER: {
                        assertThat(printFields.get(
                            acasCertificatePdfFieldModel.getNoAcasReasonNoPowerToConciliateFieldName()))
                            .contains(YES);
                        break;
                    }
                    case PdfMapperConstants.PDF_TEMPLATE_REASON_NOT_HAVING_ACAS_EMPLOYER_ALREADY_IN_TOUCH: {
                        assertThat(printFields.get(
                            acasCertificatePdfFieldModel.getNoAcasReasonEmployerContactedFieldName()))
                            .contains(YES);
                        break;
                    }
                    default: break;
                }
            }
        }
    }

    private static void checkAcasCertificateNotFoundCheckbox(AcasCertificatePdfFieldModel acasCertificatePdfFieldModel,
                                                           ConcurrentMap<String, Optional<String>> printFields) {
        if (PDF_TEMPLATE_Q2_3_1_2_FIRST_RESPONDENT_ACAS_CERTIFICATE_CHECK_NO
            .equals(acasCertificatePdfFieldModel.getAcasCertificateCheckNoFieldName())
            || PDF_TEMPLATE_Q13_2_1_2_FORTH_RESPONDENT_ACAS_CERTIFICATE_CHECK_NO
            .equals(acasCertificatePdfFieldModel.getAcasCertificateCheckNoFieldName())) {
            assertThat(printFields.get(acasCertificatePdfFieldModel.getAcasCertificateCheckNoFieldName()))
                .contains(NO_LOWERCASE);
        } else if (PDF_TEMPLATE_Q2_8_1_2_THIRD_RESPONDENT_ACAS_CERTIFICATE_CHECK_NO
            .equals(acasCertificatePdfFieldModel.getAcasCertificateCheckNoFieldName())) {
            assertThat(printFields.get(acasCertificatePdfFieldModel.getAcasCertificateCheckNoFieldName()))
                .contains(YES);
        } else {
            assertThat(printFields.get(acasCertificatePdfFieldModel.getAcasCertificateCheckNoFieldName()))
                .contains(NO);
        }
    }
}
