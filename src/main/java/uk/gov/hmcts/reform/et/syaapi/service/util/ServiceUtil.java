package uk.gov.hmcts.reform.et.syaapi.service.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.reform.et.syaapi.service.pdf.PdfDecodedMultipartFile;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;

import java.util.List;

import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.ENGLISH_LANGUAGE;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.WELSH_LANGUAGE;

@Slf4j
public final class ServiceUtil {

    private ServiceUtil() {

    }

    public static String findClaimantLanguage(CaseData caseData) {
        return caseData.getClaimantHearingPreference() != null
            && caseData.getClaimantHearingPreference().getContactLanguage() != null
            && WELSH_LANGUAGE.equals(caseData.getClaimantHearingPreference().getContactLanguage()) ? WELSH_LANGUAGE
            : ENGLISH_LANGUAGE;
    }

    public static void logException(String firstWord, String caseReferenceNumber, String errorMessage,
                             String className, String methodName) {
        log.info("*************EXCEPTION OCCURED*************"
                     + "\nERROR DESCRIPTION: " + firstWord
                     + "\nCASE REFERENCE: " + caseReferenceNumber
                     + "\nERROR MESSAGE: " + errorMessage
                     + "\nCLASS NAME: " + className
                     + "\nMETHOD NAME: " + methodName
                     + "\n*****************END OF EXCEPTION MESSAGE***********************");
    }

    public static String findClaimantFirstNameByCaseDataUserInfo(CaseData caseData, UserInfo userInfo) {
        return ObjectUtils.isNotEmpty(caseData.getClaimantIndType())
            && StringUtils.isNotBlank(caseData.getClaimantIndType().getClaimantFirstNames())
            ? caseData.getClaimantIndType().getClaimantFirstNames() : userInfo.getGivenName();
    }

    public static String findClaimantLastNameByCaseDataUserInfo(CaseData caseData, UserInfo userInfo) {
        return ObjectUtils.isNotEmpty(caseData.getClaimantIndType())
            && StringUtils.isNotBlank(caseData.getClaimantIndType().getClaimantLastName())
            ? caseData.getClaimantIndType().getClaimantLastName() : userInfo.getFamilyName();
    }

    public static boolean hasPdfFile(List<PdfDecodedMultipartFile> pdfFileList, int index) {
        return ObjectUtils.isNotEmpty(pdfFileList)
            && pdfFileList.size() > index
            && ObjectUtils.isNotEmpty(pdfFileList.get(index))
            && ObjectUtils.isNotEmpty(pdfFileList.get(index).getBytes());
    }

    public static Object prepareUpload(List<PdfDecodedMultipartFile> pdfFileList, int index)
        throws NotificationClientException {
        return hasPdfFile(pdfFileList, index)
            ? NotificationClient.prepareUpload(pdfFileList.get(index).getBytes())
            : "File does not exist";
    }

    public static byte[] findPdfFileBySelectedLanguage(List<PdfDecodedMultipartFile> pdfFileList,
                                                       String selectedLanguage) {
        if (WELSH_LANGUAGE.equals(selectedLanguage) && hasPdfFile(pdfFileList, 1)) {
            return pdfFileList.get(1).getBytes();
        } else {
            return pdfFileList.get(0).getBytes();
        }
    }

}
