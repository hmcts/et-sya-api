package uk.gov.hmcts.reform.et.syaapi.service.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import uk.gov.hmcts.et.common.model.ccd.types.DocumentType;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.LinkedHashMap;
import java.util.List;

@Slf4j
public final class DocumentUtil {

    private static final String HIDDEN_DOCUMENT_TYPES_FOR_CLAIMANT = "|ET1 Vetting|, ";


    private DocumentUtil() {
        // Utility classes should not have a public or default constructor.
    }

    public static void filterClaimantDocuments(List<CaseDetails> caseDetailsList) {
        for (CaseDetails caseDetails : caseDetailsList) {
            String caseId;
            if (ObjectUtils.isNotEmpty(caseDetails.getData().get("ethosCaseReference"))) {
                caseId = caseDetails.getData().get("ethosCaseReference").toString();
            } else {
                caseId = ObjectUtils.isNotEmpty(caseDetails.getId()) ? caseDetails.getId().toString() : "";
            }
            filterDocumentsForClaimant(caseDetails, caseId);
        }
    }

    @SuppressWarnings("unchecked")
    private static void filterDocumentsForClaimant(CaseDetails caseDetails, String caseId) {
        if (ObjectUtils.isNotEmpty(caseDetails.getData().get("documentCollection"))) {
            List<LinkedHashMap<String, Object>> documentCollection =
                (List<LinkedHashMap<String, Object>>) caseDetails.getData().get("documentCollection");
            for (LinkedHashMap<String, Object> documentTypeItem : documentCollection) {
                if (ObjectUtils.isNotEmpty(documentTypeItem.get("value"))) {
                    try {
                        DocumentType documentType = GenericServiceUtil
                            .mapJavaObjectToClass(DocumentType.class, documentTypeItem.get("value"));
                        if (isDocumentHidden(documentType)) {
                            documentCollection.remove(documentTypeItem);
                        }
                    } catch (JsonProcessingException jpe) {
                        GenericServiceUtil.logException(
                            "Exception occurred while processing documents (JSON PARSE PROBLEM)", caseId,
                            jpe.getMessage(), "filterClaimantDocuments", "DocumentUtil");
                    }
                }
            }
        }
    }

    private static boolean isDocumentHidden(DocumentType documentType) {
        return StringUtils.isNotBlank(documentType.getTypeOfDocument()) && HIDDEN_DOCUMENT_TYPES_FOR_CLAIMANT.contains(
            "|" + documentType.getTypeOfDocument().trim() + "|")
            || StringUtils.isNotBlank(documentType.getDocumentType()) && HIDDEN_DOCUMENT_TYPES_FOR_CLAIMANT.contains(
            "|" + documentType.getDocumentType().trim() + "|");
    }

}
