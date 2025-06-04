package uk.gov.hmcts.reform.et.syaapi.service.pdf;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ecm.common.exceptions.PdfServiceException;
import uk.gov.hmcts.ecm.common.service.pdf.PdfDecodedMultipartFile;
import uk.gov.hmcts.ecm.common.service.pdf.PdfService;
import uk.gov.hmcts.ecm.common.service.pdf.et1.GenericServiceUtil;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.DocumentTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.reform.et.syaapi.service.CaseDocumentException;
import uk.gov.hmcts.reform.et.syaapi.service.CaseDocumentService;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.util.ArrayList;

import static uk.gov.hmcts.ecm.common.constants.PdfMapperConstants.PDF_TYPE_ET3;
import static uk.gov.hmcts.ecm.common.service.pdf.et3.ET3FormConstants.ET3_FORM_CLIENT_TYPE_RESPONDENT;
import static uk.gov.hmcts.ecm.common.service.pdf.et3.ET3FormConstants.SUBMIT_ET3_CITIZEN;
import static uk.gov.hmcts.reform.et.syaapi.constants.DocumentCategoryConstants.ET3_PDF_DOC_CATEGORY;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.ENGLISH_LANGUAGE;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.ET3;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.PDF_FILE_TIKA_CONTENT_TYPE;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.STRING_DASH;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.WELSH_LANGUAGE;
import static uk.gov.hmcts.reform.et.syaapi.constants.ManageCaseRoleConstants.ET3_RESPONSE_LANGUAGE_PREFERENCE_WELSH;
import static uk.gov.hmcts.reform.et.syaapi.constants.ResponseConstants.ET3_FORM_DOCUMENT_TYPE;
import static uk.gov.hmcts.reform.et.syaapi.constants.ResponseConstants.UNABLE_TO_UPLOAD_DOCUMENT;
import static uk.gov.hmcts.reform.et.syaapi.service.pdf.PdfUploadService.createPdfDocumentDescriptionFromCaseData;
import static uk.gov.hmcts.reform.et.syaapi.service.pdf.PdfUploadService.sanitizePartyName;


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

    public static String createET3PdfDocumentNameFromCaseData(String documentLanguage,
                                                              UserInfo userInfo,
                                                              RespondentSumTypeItem selectedRespondent) {
        String respondentName = getRespondentNameBySelectedRespondent(selectedRespondent, userInfo);
        return ET3 + StringUtils.SPACE + STRING_DASH + StringUtils.SPACE
            + sanitizePartyName(respondentName)
            + (ENGLISH_LANGUAGE.equals(documentLanguage)
            ? StringUtils.EMPTY
            : StringUtils.SPACE + STRING_DASH + StringUtils.SPACE + documentLanguage)
            + ".pdf";
    }

    public static String getRespondentNameBySelectedRespondent(RespondentSumTypeItem selectedRespondent,
                                                               UserInfo userInfo) {
        if (ObjectUtils.isEmpty(selectedRespondent) || ObjectUtils.isEmpty(selectedRespondent.getValue())) {
            return StringUtils.EMPTY;
        }
        if (StringUtils.isNotBlank(selectedRespondent.getValue().getResponseRespondentName())) {
            return selectedRespondent.getValue().getResponseRespondentName();
        }
        if (StringUtils.isNotBlank(selectedRespondent.getValue().getRespondentOrganisation())) {
            return selectedRespondent.getValue().getRespondentOrganisation();
        }
        String respondentName = getRespondentNameByUserInfo(userInfo);

        if (StringUtils.isBlank(respondentName)) {
            respondentName = getRespondentNameByRespondentFirstAndLastNames(selectedRespondent);
        }
        return respondentName.trim();
    }

    private static String getRespondentNameByUserInfo(UserInfo userInfo) {
        if (ObjectUtils.isEmpty(userInfo)) {
            return StringUtils.EMPTY;
        }
        String respondentName = StringUtils.EMPTY;
        if (StringUtils.isNotBlank(userInfo.getGivenName())) {
            respondentName = userInfo.getGivenName();
        }
        if (ObjectUtils.isNotEmpty(userInfo) && StringUtils.isNotBlank(userInfo.getFamilyName())) {
            respondentName = respondentName + StringUtils.SPACE + userInfo.getFamilyName();
        }
        return respondentName.trim();
    }

    private static String getRespondentNameByRespondentFirstAndLastNames(RespondentSumTypeItem respondent) {
        if (ObjectUtils.isEmpty(respondent)) {
            return StringUtils.EMPTY;
        }
        String respondentName = StringUtils.EMPTY;
        if (StringUtils.isNotBlank(respondent.getValue().getRespondentFirstName())) {
            respondentName = respondent.getValue().getRespondentFirstName();
        }
        if (StringUtils.isNotBlank(respondent.getValue().getRespondentLastName())) {
            respondentName = respondentName
                + StringUtils.SPACE
                + respondent.getValue().getRespondentLastName();
        }
        return respondentName.trim();
    }

    public void generateET3WelshAndEnglishForms(String authorisation,
                                                CaseData caseData,
                                                RespondentSumTypeItem selectedRespondent) {
        log.info("Generating ET3 Welsh and English Forms");
        try {
            caseData.setSubmitEt3Respondent(DynamicFixedListType.from("SubmitRespondent",
                                                                      selectedRespondent.getId(),
                                                                      true));
            byte[] englishPdfFileByteArray = pdfService.convertCaseToPdf(
                caseData,
                et3EnglishPdfTemplateSource,
                PDF_TYPE_ET3,
                ET3_FORM_CLIENT_TYPE_RESPONDENT,
                SUBMIT_ET3_CITIZEN
            );
            UserInfo userInfo = idamClient.getUserInfo(authorisation);
            PdfDecodedMultipartFile englishET3Form = new PdfDecodedMultipartFile(
                englishPdfFileByteArray,
                createET3PdfDocumentNameFromCaseData(ENGLISH_LANGUAGE, userInfo, selectedRespondent),
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
            if (CollectionUtils.isEmpty(caseData.getDocumentCollection())) {
                caseData.setDocumentCollection(new ArrayList<>());
            }
            selectedRespondent.getValue().setEt3Form(englishDocument.getValue().getUploadedDocument());
            if (ET3_RESPONSE_LANGUAGE_PREFERENCE_WELSH.equals(
                selectedRespondent.getValue().getEt3ResponseLanguagePreference())) {
                byte[] welshPdfFileByteArray = pdfService.convertCaseToPdf(
                    caseData,
                    et3WelshPdfTemplateSource,
                    PDF_TYPE_ET3,
                    ET3_FORM_CLIENT_TYPE_RESPONDENT,
                    SUBMIT_ET3_CITIZEN
                );
                PdfDecodedMultipartFile welshET3Form = new PdfDecodedMultipartFile(
                    welshPdfFileByteArray,
                    createET3PdfDocumentNameFromCaseData(WELSH_LANGUAGE, userInfo, selectedRespondent),
                    PDF_FILE_TIKA_CONTENT_TYPE,
                    createPdfDocumentDescriptionFromCaseData(caseData)
                );
                DocumentTypeItem welshDocument = caseDocumentService.createDocumentTypeItem(
                    authorisation,
                    caseData.getEcmCaseType(),
                    ET3_FORM_DOCUMENT_TYPE,
                    ET3_PDF_DOC_CATEGORY,
                    welshET3Form
                );
                selectedRespondent.getValue().setEt3FormWelsh(welshDocument.getValue().getUploadedDocument());
            }
        } catch (PdfServiceException | CaseDocumentException e) {
            GenericServiceUtil.logException(UNABLE_TO_UPLOAD_DOCUMENT,
                                            caseData.getEthosCaseReference(),
                                            e.getMessage(),
                                            "ET3FormService",
                                            "generateET3WelshAndEnglishForms");
        }
    }
}
