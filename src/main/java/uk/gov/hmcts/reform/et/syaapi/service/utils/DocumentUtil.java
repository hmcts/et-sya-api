package uk.gov.hmcts.reform.et.syaapi.service.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import uk.gov.hmcts.et.common.model.ccd.types.DocumentType;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;

@Slf4j
public final class DocumentUtil {

    private static final String HIDDEN_DOCUMENT_TYPES_FOR_CLAIMANT = "|ET1 Vetting|, "
        + "|ET3 Processing|"
        + "|Referral/Judicial Direction|"
        + "|App for a Witness Order - R|"
        + "|App for a Witness Order - R|"
        + "|Contact the tribunal about something else - C|"
        + "|Contact the tribunal about something else - R|"
        + "|COT3|"
        + "|Tribunal case file|";

    private DocumentUtil() {
        // Utility classes should not have a public or default constructor.
    }

    public static void filterMultipleCasesDocumentsForClaimant(List<CaseDetails> caseDetailsList) {
        for (CaseDetails caseDetails : caseDetailsList) {
            String caseId;
            if (ObjectUtils.isNotEmpty(caseDetails.getData())
                && ObjectUtils.isNotEmpty(caseDetails.getData().get("ethosCaseReference"))) {
                caseId = caseDetails.getData().get("ethosCaseReference").toString();
            } else {
                caseId = ObjectUtils.isNotEmpty(caseDetails.getId()) ? caseDetails.getId().toString() : "";
            }
            filterCaseDocumentsForClaimant(caseDetails, caseId);
        }
    }

    @SuppressWarnings("unchecked")
    public static void filterCaseDocumentsForClaimant(CaseDetails caseDetails, String caseId) {
        if (ObjectUtils.isNotEmpty(caseDetails.getData())
            && ObjectUtils.isNotEmpty(caseDetails.getData().get("documentCollection"))) {
            List<LinkedHashMap<String, Object>> documentCollection =
                (List<LinkedHashMap<String, Object>>) caseDetails.getData().get("documentCollection");
            for (Iterator<LinkedHashMap<String, Object>> iterator = documentCollection.iterator();
                 iterator.hasNext();) {
                removeHiddenDocumentFromCollection(iterator, caseId);
            }
        }
    }

    private static void removeHiddenDocumentFromCollection(Iterator<LinkedHashMap<String, Object>> iterator,
                                                           String caseId) {
        LinkedHashMap<String, Object> documentInfo = iterator.next();
        if (ObjectUtils.isNotEmpty(documentInfo.get("value"))) {
            try {
                DocumentType documentType = GenericServiceUtil
                    .mapJavaObjectToClass(DocumentType.class, documentInfo.get("value"));
                if (isDocumentHidden(documentType)) {
                    iterator.remove();
                }
            } catch (JsonProcessingException jpe) {
                GenericServiceUtil.logException(
                    "Exception occurred while processing documents (JSON PARSE PROBLEM)", caseId,
                    jpe.getMessage(), "filterClaimantDocuments", "DocumentUtil");
            }
        }
    }

    private static boolean isDocumentHidden(DocumentType documentType) {
        return StringUtils.isNotBlank(documentType.getTypeOfDocument())
            && HIDDEN_DOCUMENT_TYPES_FOR_CLAIMANT.toLowerCase(Locale.UK).contains(
            "|" + documentType.getTypeOfDocument().toLowerCase(Locale.UK).trim() + "|")
            || StringUtils.isNotBlank(documentType.getDocumentType())
            && HIDDEN_DOCUMENT_TYPES_FOR_CLAIMANT.toLowerCase(Locale.UK).contains(
            "|" + documentType.getDocumentType().toLowerCase(Locale.UK).trim() + "|");
    }

}
