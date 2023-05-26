package uk.gov.hmcts.reform.et.syaapi.service.utils;

import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.common.Strings;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import uk.gov.hmcts.et.common.model.ccd.Address;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.types.RespondentSumType;
import uk.gov.hmcts.reform.et.syaapi.models.AcasCertificatePdfFieldModel;
import uk.gov.hmcts.reform.et.syaapi.models.RespondentPdfFieldModel;
import uk.gov.hmcts.reform.et.syaapi.service.pdf.PdfServiceException;

import java.util.Optional;
import java.util.concurrent.ConcurrentMap;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.PdfMapperConstants.NO;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.PdfMapperConstants.NO_LOWERCASE;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.PdfMapperConstants.PDF_TEMPLATE_MULTIPLE_RESPONDENTS_MIN_NUMBER;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.PdfMapperConstants.PDF_TEMPLATE_Q2_4_1_CLAIMANT_WORK_ADDRESS;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.PdfMapperConstants.PDF_TEMPLATE_Q2_4_2_CLAIMANT_WORK_POSTCODE;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.PdfMapperConstants.PDF_TEMPLATE_Q2_5_MULTIPLE_RESPONDENTS;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.PdfMapperConstants.YES;

public final class PdfMapperRespondentUtil {

    private PdfMapperRespondentUtil() {
        // Utility classes should not have a public or default constructor.
    }

    public static void putRespondents(CaseData caseData, ConcurrentMap<String, Optional<String>> printFields) {

        if (!CollectionUtils.isEmpty(caseData.getRespondentCollection())) {
            try {
                putFirstRespondent(caseData,
                                   PdfTemplateRespondentFieldNamesEnum.FIRST_RESPONDENT.respondentPdfFieldModel,
                                   printFields);
                if (caseData.getRespondentCollection().size()
                    >= PDF_TEMPLATE_MULTIPLE_RESPONDENTS_MIN_NUMBER) {
                    printFields.put(PDF_TEMPLATE_Q2_5_MULTIPLE_RESPONDENTS,
                                    Optional.of(YES));
                    // Put Second Respondent if exists
                    putRespondent(caseData.getRespondentCollection().get(1).getValue(),
                                  PdfTemplateRespondentFieldNamesEnum.SECOND_RESPONDENT.respondentPdfFieldModel,
                                  printFields);
                    // put third respondent if exists
                    putRespondent(
                        caseData.getRespondentCollection().get(2).getValue(),
                        PdfTemplateRespondentFieldNamesEnum.THIRD_RESPONDENT.respondentPdfFieldModel,
                        printFields);
                    // put fourth respondent if exists
                    putRespondent(
                        caseData.getRespondentCollection().get(3).getValue(),
                        PdfTemplateRespondentFieldNamesEnum.FORTH_RESPONDENT.respondentPdfFieldModel,
                        printFields);
                    // put fifth respondent if exists
                    putRespondent(
                        caseData.getRespondentCollection().get(4).getValue(),
                        PdfTemplateRespondentFieldNamesEnum.FIFTH_RESPONDENT.respondentPdfFieldModel,
                        printFields);
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

        if (NO.equals(caseData.getClaimantWorkAddressQuestion())
            && !ObjectUtils.isEmpty(caseData.getClaimantWorkAddress())) {
            putAddress(caseData.getClaimantWorkAddress().getClaimantWorkAddress(),
                                          PDF_TEMPLATE_Q2_4_1_CLAIMANT_WORK_ADDRESS,
                                          PDF_TEMPLATE_Q2_4_2_CLAIMANT_WORK_POSTCODE,
                                          printFields);
        }
    }

    private static void putRespondent(RespondentSumType respondent,
                                      RespondentPdfFieldModel respondentPdfFieldModel,
                                      ConcurrentMap<String, Optional<String>> printFields) throws PdfServiceException {
        if (!ObjectUtils.isEmpty(respondent)) {
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
                    case "Unfair Dismissal": {
                        printFields.put(
                            acasCertificatePdfModel.getNoAcasReasonUnfairDismissalFieldName(),
                            Optional.of(YES)
                        );
                        break;
                    }
                    case "Another person": {
                        printFields.put(
                            acasCertificatePdfModel.getNoAcasReasonAnotherPersonFieldName(),
                            Optional.of(YES)
                        );
                        break;
                    }
                    case "No Power": {
                        printFields.put(
                            acasCertificatePdfModel.getNoAcasReasonNoPowerToConciliateFieldName(),
                            Optional.of(YES)
                        );
                        break;
                    }
                    case "Employer already in touch": {
                        printFields.put(
                            acasCertificatePdfModel.getNoAcasReasonEmployerContactedFieldName(),
                            Optional.of(YES)
                        );
                        break;
                    }
                    default: {
                        throw new PdfServiceException("Invalid No ACAS Certificate Reason Selected!...",
                                                             new Exception("Error while creating PDF document!..."));
                    }
                }
            }
        }
    }

}
