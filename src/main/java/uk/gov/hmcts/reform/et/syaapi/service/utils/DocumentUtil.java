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
import java.util.stream.Stream;

import static uk.gov.hmcts.ecm.common.model.helper.DocumentConstants.APP_FOR_A_WITNESS_ORDER_R;
import static uk.gov.hmcts.ecm.common.model.helper.DocumentConstants.CLAIMANT_APPLICATION_DOC_TYPE;
import static uk.gov.hmcts.ecm.common.model.helper.DocumentConstants.CONTACT_THE_TRIBUNAL_R;
import static uk.gov.hmcts.ecm.common.model.helper.DocumentConstants.COT3;
import static uk.gov.hmcts.ecm.common.model.helper.DocumentConstants.ET1_VETTING;
import static uk.gov.hmcts.ecm.common.model.helper.DocumentConstants.ET3_PROCESSING;
import static uk.gov.hmcts.ecm.common.model.helper.DocumentConstants.INITIAL_CONSIDERATION;
import static uk.gov.hmcts.ecm.common.model.helper.DocumentConstants.OTHER;
import static uk.gov.hmcts.ecm.common.model.helper.DocumentConstants.REFERRAL_JUDICIAL_DIRECTION;
import static uk.gov.hmcts.ecm.common.model.helper.DocumentConstants.RESPONDENT_APPLICATION_DOC_TYPE;
import static uk.gov.hmcts.ecm.common.model.helper.DocumentConstants.TRIBUNAL_CASE_FILE;

@Slf4j
public final class DocumentUtil {

    private static final List<String> HIDDEN_DOCUMENT_TYPES_FOR_CLAIMANT = List.of(
        ET1_VETTING,
        ET3_PROCESSING,
        REFERRAL_JUDICIAL_DIRECTION,
        APP_FOR_A_WITNESS_ORDER_R,
        CONTACT_THE_TRIBUNAL_R,
        COT3,
        TRIBUNAL_CASE_FILE,
        INITIAL_CONSIDERATION,
        OTHER
    );

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
        return isHiddenDocumentType(documentType.getTypeOfDocument())
            || isHiddenDocumentType(documentType.getDocumentType());
    }

    private static boolean isHiddenDocumentType(String documentType) {
        if (StringUtils.isNotBlank(documentType)) {
            String lowerCaseDocumentType = documentType.toLowerCase(Locale.UK).trim();
            List<String> mergedList = getMergedList();
            return mergedList.stream()
                .map(type -> type.toLowerCase(Locale.UK))
                .anyMatch(type -> type.equals(lowerCaseDocumentType));
        }
        return false;
    }

    private static List<String> getMergedList() {
        return Stream.of(
                HIDDEN_DOCUMENT_TYPES_FOR_CLAIMANT,
                RESPONDENT_APPLICATION_DOC_TYPE,
                CLAIMANT_APPLICATION_DOC_TYPE
            )
            .flatMap(List::stream)
            .distinct()
            .toList();
    }

}
