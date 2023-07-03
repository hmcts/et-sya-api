package uk.gov.hmcts.reform.et.syaapi.service.util;

import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.common.Strings;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import uk.gov.hmcts.et.common.model.ccd.Address;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.RespondentSumType;
import uk.gov.hmcts.reform.et.syaapi.exception.PdfServiceException;
import uk.gov.hmcts.reform.et.syaapi.model.AcasCertificatePdfFieldModel;
import uk.gov.hmcts.reform.et.syaapi.model.RespondentPdfFieldModel;

import java.util.Optional;
import java.util.concurrent.ConcurrentMap;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.et.syaapi.service.util.PdfMapperConstants.INVALID_NO_ACAS_REASON;
import static uk.gov.hmcts.reform.et.syaapi.service.util.PdfMapperConstants.MAX_NUMBER_OF_RESPONDENTS;
import static uk.gov.hmcts.reform.et.syaapi.service.util.PdfMapperConstants.NO;
import static uk.gov.hmcts.reform.et.syaapi.service.util.PdfMapperConstants.NO_LOWERCASE;
import static uk.gov.hmcts.reform.et.syaapi.service.util.PdfMapperConstants.NUMERIC_FOUR_INT_VALUE;
import static uk.gov.hmcts.reform.et.syaapi.service.util.PdfMapperConstants.NUMERIC_THREE_INT_VALUE;
import static uk.gov.hmcts.reform.et.syaapi.service.util.PdfMapperConstants.PDF_CREATION_ERROR;
import static uk.gov.hmcts.reform.et.syaapi.service.util.PdfMapperConstants.PDF_TEMPLATE_MULTIPLE_RESPONDENTS_MIN_NUMBER;
import static uk.gov.hmcts.reform.et.syaapi.service.util.PdfMapperConstants.PDF_TEMPLATE_Q2_4_1_CLAIMANT_WORK_ADDRESS;
import static uk.gov.hmcts.reform.et.syaapi.service.util.PdfMapperConstants.PDF_TEMPLATE_Q2_4_2_CLAIMANT_WORK_POSTCODE;
import static uk.gov.hmcts.reform.et.syaapi.service.util.PdfMapperConstants.PDF_TEMPLATE_Q2_5_MULTIPLE_RESPONDENTS;
import static uk.gov.hmcts.reform.et.syaapi.service.util.PdfMapperConstants.PDF_TEMPLATE_REASON_NOT_HAVING_ACAS_ANOTHER_PERSON;
import static uk.gov.hmcts.reform.et.syaapi.service.util.PdfMapperConstants.PDF_TEMPLATE_REASON_NOT_HAVING_ACAS_EMPLOYER_ALREADY_IN_TOUCH;
import static uk.gov.hmcts.reform.et.syaapi.service.util.PdfMapperConstants.PDF_TEMPLATE_REASON_NOT_HAVING_ACAS_NO_POWER;
import static uk.gov.hmcts.reform.et.syaapi.service.util.PdfMapperConstants.PDF_TEMPLATE_REASON_NOT_HAVING_ACAS_UNFAIR_DISMISSAL;
import static uk.gov.hmcts.reform.et.syaapi.service.util.PdfMapperConstants.YES;

public final class PdfMapperRespondentUtil {

    private PdfMapperRespondentUtil() {
        // Utility classes should not have a public or default constructor.
    }

    public static void putRespondents(CaseData caseData, ConcurrentMap<String, Optional<String>> printFields) {

        if (ObjectUtils.isEmpty(caseData) || CollectionUtils.isEmpty(caseData.getRespondentCollection())) {
            putClaimantWorkAddress(caseData, printFields);
        } else {
            try {
                putFirstRespondent(caseData,
                                   PdfTemplateRespondentFieldNamesEnum.FIRST_RESPONDENT.respondentPdfFieldModel,
                                   printFields);
                if (caseData.getRespondentCollection().size()
                    >= PDF_TEMPLATE_MULTIPLE_RESPONDENTS_MIN_NUMBER) {
                    printFields.put(
                        PDF_TEMPLATE_Q2_5_MULTIPLE_RESPONDENTS,
                        Optional.of(YES)
                    );
                    // Put Second Respondent
                    putRespondent(
                        caseData.getRespondentCollection().get(1),
                        PdfTemplateRespondentFieldNamesEnum.SECOND_RESPONDENT.respondentPdfFieldModel,
                        printFields
                    );
                }
                // put third respondent if exists
                if (caseData.getRespondentCollection().size() >= NUMERIC_THREE_INT_VALUE) {
                    putRespondent(
                        caseData.getRespondentCollection().get(2),
                        PdfTemplateRespondentFieldNamesEnum.THIRD_RESPONDENT.respondentPdfFieldModel,
                        printFields
                    );
                }
                // put fourth respondent if exists
                if (caseData.getRespondentCollection().size() >= NUMERIC_FOUR_INT_VALUE) {
                    putRespondent(
                        caseData.getRespondentCollection().get(3),
                        PdfTemplateRespondentFieldNamesEnum.FORTH_RESPONDENT.respondentPdfFieldModel,
                        printFields
                    );
                }
                // put fifth respondent if exists
                if (caseData.getRespondentCollection().size() == MAX_NUMBER_OF_RESPONDENTS) {
                    putRespondent(
                        caseData.getRespondentCollection().get(4),
                        PdfTemplateRespondentFieldNamesEnum.FIFTH_RESPONDENT.respondentPdfFieldModel,
                        printFields
                    );
                }
            } catch (PdfServiceException pse) {
                GenericServiceUtil.logException("Error while creating PDF file", caseData.getEthosCaseReference(),
                                                pse.getMessage(), "PDFMapperRespondentUtil", "putRespondents");
            }
        }
    }

    private static void putFirstRespondent(CaseData caseData,
                                          RespondentPdfFieldModel firstRespondentPdfFieldModel,
                                          ConcurrentMap<String, Optional<String>> printFields)
        throws PdfServiceException {
        RespondentSumType firstRespondent = caseData.getRespondentCollection().get(0).getValue();
        printFields.put(
            firstRespondentPdfFieldModel.respondentNameFieldName(), ofNullable(firstRespondent.getRespondentName()));
        putAddress(firstRespondent.getRespondentAddress(),
                   firstRespondentPdfFieldModel.respondentAddressFieldName(),
                   firstRespondentPdfFieldModel.respondentPostcodeFieldName(),
                   printFields);
        putAcasCertificateDetails(firstRespondent,
                                  firstRespondentPdfFieldModel.respondentAcasCertificatePdfFieldModel(),
                                  printFields);
        putClaimantWorkAddress(caseData, printFields);
    }

    private static void putClaimantWorkAddress(CaseData caseData, ConcurrentMap<String, Optional<String>> printFields) {
        if (!ObjectUtils.isEmpty(caseData)
            && NO.equals(caseData.getClaimantWorkAddressQuestion())
            && !ObjectUtils.isEmpty(caseData.getClaimantWorkAddress())) {
            putAddress(caseData.getClaimantWorkAddress().getClaimantWorkAddress(),
                       PDF_TEMPLATE_Q2_4_1_CLAIMANT_WORK_ADDRESS,
                       PDF_TEMPLATE_Q2_4_2_CLAIMANT_WORK_POSTCODE,
                       printFields);
        }
    }

    private static void putRespondent(RespondentSumTypeItem respondentItem,
                                      RespondentPdfFieldModel respondentPdfFieldModel,
                                      ConcurrentMap<String, Optional<String>> printFields) throws PdfServiceException {
        if (!ObjectUtils.isEmpty(respondentItem)) {
            RespondentSumType respondent = respondentItem.getValue();
            printFields.put(
                respondentPdfFieldModel.respondentNameFieldName(),
                ofNullable(respondent.getRespondentName())
            );
            putAddress(
                respondent.getRespondentAddress(),
                respondentPdfFieldModel.respondentAddressFieldName(),
                respondentPdfFieldModel.respondentPostcodeFieldName(),
                printFields
            );
            putAcasCertificateDetails(
                respondent,
                respondentPdfFieldModel.respondentAcasCertificatePdfFieldModel(),
                printFields
            );
        }
    }

    private static void putAddress(Address address, String addressField, String postCodeField,
                            ConcurrentMap<String, Optional<String>> printFields) {
        if (!ObjectUtils.isEmpty(address)) {
            printFields.put(
                addressField,
                ofNullable(PdfMapperServiceUtil.formatAddressForTextField(address))
            );
            printFields.put(
                postCodeField,
                ofNullable(PdfMapperServiceUtil.formatUkPostcode(address))
            );
        }
    }

    private static void putAcasCertificateDetails(RespondentSumType respondent,
                                                  AcasCertificatePdfFieldModel acasCertificatePdfModel,
                                                  ConcurrentMap<String, Optional<String>> printFields)
        throws PdfServiceException {
        if (StringUtils.isNotBlank(respondent.getRespondentAcasQuestion())
            && YES.equals(respondent.getRespondentAcasQuestion())) {
            printFields.put(acasCertificatePdfModel.getAcasCertificateCheckYesFieldName(),
                            Optional.of(YES));
            printFields.put(acasCertificatePdfModel.getAcasCertificateNumberFieldName(),
                            ofNullable(respondent.getRespondentAcas())
            );
        } else {
            printFields.put(acasCertificatePdfModel.getAcasCertificateCheckNoFieldName(),
                            Optional.of(NO_LOWERCASE));
            if (!Strings.isNullOrEmpty(respondent.getRespondentAcasNo())) {
                switch (respondent.getRespondentAcasNo()) {
                    case PDF_TEMPLATE_REASON_NOT_HAVING_ACAS_UNFAIR_DISMISSAL: {
                        printFields.put(
                            acasCertificatePdfModel.getNoAcasReasonUnfairDismissalFieldName(),
                            Optional.of(YES)
                        );
                        break;
                    }
                    case PDF_TEMPLATE_REASON_NOT_HAVING_ACAS_ANOTHER_PERSON: {
                        printFields.put(
                            acasCertificatePdfModel.getNoAcasReasonAnotherPersonFieldName(),
                            Optional.of(YES)
                        );
                        break;
                    }
                    case PDF_TEMPLATE_REASON_NOT_HAVING_ACAS_NO_POWER: {
                        printFields.put(
                            acasCertificatePdfModel.getNoAcasReasonNoPowerToConciliateFieldName(),
                            Optional.of(YES)
                        );
                        break;
                    }
                    case PDF_TEMPLATE_REASON_NOT_HAVING_ACAS_EMPLOYER_ALREADY_IN_TOUCH: {
                        printFields.put(
                            acasCertificatePdfModel.getNoAcasReasonEmployerContactedFieldName(),
                            Optional.of(YES)
                        );
                        break;
                    }
                    default: {
                        throw new PdfServiceException(INVALID_NO_ACAS_REASON, new Exception(PDF_CREATION_ERROR));
                    }
                }
            }
        }
    }

}
