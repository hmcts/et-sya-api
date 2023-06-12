package uk.gov.hmcts.reform.et.syaapi.service.utils;

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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.ADDRESS_LINE_1;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.ADDRESS_LINE_2;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.ADDRESS_LINE_3;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.COUNTRY;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.COUNTY;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.INTEGER_NUMERIC_FIVE;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.INTEGER_NUMERIC_FOUR;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.INTEGER_NUMERIC_THREE;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.INTEGER_NUMERIC_TWO;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.NO;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.NULL_ADDRESS;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.NULL_STRING;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.POSTCODE;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.POST_TOWN;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.STRING_NUMERIC_ONE;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.TEST_COMPANY_NAME;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.YES;

class PdfMapperRespondentUtilTest {

    @ParameterizedTest
    @NullSource
    @MethodSource(
        "uk.gov.hmcts.reform.et.syaapi.model.PdfMapperTestData#generateCaseDataSamplesWithRespondentSumTypeItems")
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
            if (respondentCaseData.getRespondentCollection().size() >= INTEGER_NUMERIC_TWO) {
                RespondentPdfFieldModel secondRespondentPdfFieldModel =
                    PdfTemplateRespondentFieldNamesEnum.SECOND_RESPONDENT.respondentPdfFieldModel;
                RespondentSumType secondRespondent = respondentCaseData.getRespondentCollection().get(1).getValue();
                checkRespondentNameAddress(printFields, secondRespondentPdfFieldModel, secondRespondent);
                checkAcasCertificate(printFields,
                                     secondRespondent,
                                     secondRespondentPdfFieldModel.respondentAcasCertificatePdfFieldModel());
            }
            if (respondentCaseData.getRespondentCollection().size() >= INTEGER_NUMERIC_THREE) {
                RespondentPdfFieldModel thirdRespondentPdfFieldModel =
                    PdfTemplateRespondentFieldNamesEnum.THIRD_RESPONDENT.respondentPdfFieldModel;
                RespondentSumType thirdRespondent = respondentCaseData.getRespondentCollection().get(2).getValue();
                checkRespondentNameAddress(printFields, thirdRespondentPdfFieldModel, thirdRespondent);
                checkAcasCertificate(printFields,
                                     thirdRespondent,
                                     thirdRespondentPdfFieldModel.respondentAcasCertificatePdfFieldModel());
            }
            if (respondentCaseData.getRespondentCollection().size() >= INTEGER_NUMERIC_FOUR) {
                RespondentPdfFieldModel forthRespondentPdfFieldModel =
                    PdfTemplateRespondentFieldNamesEnum.FORTH_RESPONDENT.respondentPdfFieldModel;
                RespondentSumType forthRespondent = respondentCaseData.getRespondentCollection().get(3).getValue();
                checkRespondentNameAddress(printFields, forthRespondentPdfFieldModel, forthRespondent);
                checkAcasCertificate(printFields,
                                     forthRespondent,
                                     forthRespondentPdfFieldModel.respondentAcasCertificatePdfFieldModel());
            }
            if (respondentCaseData.getRespondentCollection().size() >= INTEGER_NUMERIC_FIVE) {
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
            Address respondentAddress = TestUtil.generateAddressByAddressFields(ADDRESS_LINE_1, ADDRESS_LINE_2,
                                                                                ADDRESS_LINE_3, POST_TOWN, COUNTY,
                                                                                COUNTRY, POSTCODE);
            CaseData respondentCaseData =
                TestUtil.generateCaseDataForRespondent(STRING_NUMERIC_ONE, YES, NULL_ADDRESS);
            RespondentSumTypeItem respondentSumTypeItem =
                TestUtil.generateRespondentSumTypeItem(STRING_NUMERIC_ONE, TEST_COMPANY_NAME,
                                                       respondentAddress,
                                                       NO,
                                                       NULL_STRING,
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
            assertThat(printFields.get(acasCertificatePdfFieldModel.getAcasCertificateCheckNoFieldName()))
                .contains(PdfMapperConstants.NO_LOWERCASE);
            if (!Strings.isNullOrEmpty(respondent.getRespondentAcasNo())) {
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
}
