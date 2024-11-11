package uk.gov.hmcts.reform.et.syaapi.service.pdf;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ecm.common.exceptions.PdfServiceException;
import uk.gov.hmcts.ecm.common.service.pdf.PdfDecodedMultipartFile;
import uk.gov.hmcts.ecm.common.service.pdf.PdfService;
import uk.gov.hmcts.ecm.common.service.pdf.et1.GenericServiceUtil;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicValueType;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.DocumentTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.reform.et.syaapi.service.CaseDocumentException;
import uk.gov.hmcts.reform.et.syaapi.service.CaseDocumentService;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.ecm.common.constants.PdfMapperConstants.PDF_TYPE_ET3;
import static uk.gov.hmcts.ecm.common.service.pdf.et3.ET3FormConstants.ET3_FORM_CLIENT_TYPE_RESPONDENT;
import static uk.gov.hmcts.reform.et.syaapi.constants.DocumentCategoryConstants.ET3_PDF_DOC_CATEGORY;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.ENGLISH_LANGUAGE;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.ET3;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.PDF_FILE_TIKA_CONTENT_TYPE;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.WELSH_LANGUAGE;
import static uk.gov.hmcts.reform.et.syaapi.constants.ResponseConstants.ET3_FORM_DOCUMENT_TYPE;
import static uk.gov.hmcts.reform.et.syaapi.constants.ResponseConstants.UNABLE_TO_UPLOAD_DOCUMENT;
import static uk.gov.hmcts.reform.et.syaapi.service.pdf.PdfUploadService.createPdfDocumentDescriptionFromCaseData;
import static uk.gov.hmcts.reform.et.syaapi.service.pdf.PdfUploadService.createPdfDocumentNameFromCaseData;

@Slf4j
@Service
@RequiredArgsConstructor()
public class ET3FormService {
    private final PdfService pdfService;
    private final CaseDocumentService caseDocumentService;
    private final IdamClient idamClient;

    @Value("${pdf.et3English}")
    public String et3EnglishPdfTemplateSource;
    @Value("${pdf.et3Welsh}")
    public String et3WelshPdfTemplateSource;

    public void generateET3WelshAndEnglishForms(String authorisation,
                                                CaseData caseData,
                                                RespondentSumTypeItem selectedRespondent) {
        log.info("Generating ET3 Welsh and English Forms");
        try {
            DynamicFixedListType dynamicFixedListType = new DynamicFixedListType();
            DynamicValueType dynamicValueType = new DynamicValueType();
            dynamicValueType.setCode("SubmitRespondent");
            dynamicValueType.setLabel(selectedRespondent.getId());

            caseData.setSubmitEt3Respondent(dynamicFixedListType);
            caseData.getSubmitEt3Respondent().setValue(dynamicValueType);
            byte[] englishPdfFileByteArray = pdfService.convertCaseToPdf(
                caseData,
                et3EnglishPdfTemplateSource,
                PDF_TYPE_ET3,
                ET3_FORM_CLIENT_TYPE_RESPONDENT
            );
            byte[] welshPdfFileByteArray = pdfService.convertCaseToPdf(
                caseData,
                et3WelshPdfTemplateSource,
                PDF_TYPE_ET3,
                ET3_FORM_CLIENT_TYPE_RESPONDENT
            );
            UserInfo userInfo = idamClient.getUserInfo(authorisation);
            PdfDecodedMultipartFile englishET3Form = new PdfDecodedMultipartFile(
                englishPdfFileByteArray,
                createPdfDocumentNameFromCaseData(caseData, ENGLISH_LANGUAGE, userInfo, ET3),
                PDF_FILE_TIKA_CONTENT_TYPE,
                createPdfDocumentDescriptionFromCaseData(caseData)
            );
            PdfDecodedMultipartFile welshET3Form = new PdfDecodedMultipartFile(
                welshPdfFileByteArray,
                createPdfDocumentNameFromCaseData(caseData, WELSH_LANGUAGE, userInfo, ET3),
                PDF_FILE_TIKA_CONTENT_TYPE,
                createPdfDocumentDescriptionFromCaseData(caseData)
            );
            DocumentTypeItem englishDocument = caseDocumentService.createDocumentTypeItem(
                authorisation,
                caseData.getEcmCaseType(),
                ET3_FORM_DOCUMENT_TYPE,
                ET3_PDF_DOC_CATEGORY,
                englishET3Form
            );

            DocumentTypeItem welshDocument = caseDocumentService.createDocumentTypeItem(
                authorisation,
                caseData.getEcmCaseType(),
                ET3_FORM_DOCUMENT_TYPE,
                ET3_PDF_DOC_CATEGORY,
                welshET3Form
            );
            if (CollectionUtils.isEmpty(caseData.getDocumentCollection())) {
                caseData.setDocumentCollection(new ArrayList<>());
            }
            caseData.getDocumentCollection().addAll(List.of(englishDocument, welshDocument));
            selectedRespondent.getValue().setEt3Form(englishDocument.getValue().getUploadedDocument());
            selectedRespondent.getValue().setEt3FormWelsh(welshDocument.getValue().getUploadedDocument());
        } catch (PdfServiceException | CaseDocumentException e) {
            GenericServiceUtil.logException(UNABLE_TO_UPLOAD_DOCUMENT,
                                            caseData.getEthosCaseReference(),
                                            e.getMessage(),
                                            "ET3FormService",
                                            "generateET3WelshAndEnglishForms");
        }
    }
}
