package uk.gov.hmcts.reform.et.syaapi.service.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import uk.gov.hmcts.ecm.common.service.pdf.PdfDecodedMultipartFile;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.ENGLISH_LANGUAGE;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.FILE_NOT_EXISTS;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.WELSH_LANGUAGE;

@Slf4j
public final class GenericServiceUtil {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private GenericServiceUtil() {
        // Utility classes should not have a public or default constructor.
    }

    public static String findClaimantLanguage(CaseData caseData) {
        return caseData.getClaimantHearingPreference() != null
            && caseData.getClaimantHearingPreference().getContactLanguage() != null
            && WELSH_LANGUAGE.equals(caseData.getClaimantHearingPreference().getContactLanguage()) ? WELSH_LANGUAGE
            : ENGLISH_LANGUAGE;
    }

    public static void logException(String firstWord, String caseReferenceNumber, String errorMessage,
                             String className, String methodName) {
        log.error("*************EXCEPTION OCCURED*************"
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
            : FILE_NOT_EXISTS;
    }

    public static byte[] findPdfFileBySelectedLanguage(List<PdfDecodedMultipartFile> pdfFileList,
                                                       String selectedLanguage) {
        if (WELSH_LANGUAGE.equals(selectedLanguage)) {
            if (hasPdfFile(pdfFileList, 1)) {
                return pdfFileList.get(1).getBytes();
            } else {
                return new byte[0];
            }
        } else {
            if (hasPdfFile(pdfFileList, 0)) {
                return pdfFileList.getFirst().getBytes();
            } else {
                return new byte[0];
            }
        }
    }

    public static String getStringValueFromStringMap(Map<String, String> parameters, String key) {
        return ObjectUtils.isEmpty(parameters.get(key)) ? "" :
            parameters.get(key);
    }

    /**
     * Converts any collection to a List of target class.
     * @param clazz Target class
     * @param rawCollection source collection
     * @param <T> Type of return list
     * @return List of given clazz type objects
     * @throws JsonProcessingException exception may occur while converting object to
     *                                 json string in mapJavaObjectToClass method
     */
    public static <T> List<T> castList(Class<? extends T> clazz, Collection<?> rawCollection)
        throws JsonProcessingException {
        List<T> result = new ArrayList<>(rawCollection.size());
        for (Object o : rawCollection) {
            result.add(mapJavaObjectToClass(clazz, o));
        }
        return result;
    }

    /**
     * Converts any object to target class.
     * @param clazz Target class
     * @param object source object
     * @param <T> Type of return object
     * @return object of given class type
     * @throws JsonProcessingException exception may occur while converting object to json string
     */
    public static <T> T mapJavaObjectToClass(Class<? extends T> clazz, Object object)
        throws JsonProcessingException {
        return OBJECT_MAPPER.readValue(OBJECT_MAPPER.writeValueAsString(object),
                                       OBJECT_MAPPER.getTypeFactory().constructType(clazz));
    }
}
