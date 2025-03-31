package uk.gov.hmcts.reform.et.syaapi.service.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import uk.gov.hmcts.ecm.common.model.helper.DocumentConstants;
import uk.gov.hmcts.et.common.model.ccd.types.DocumentType;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.et.syaapi.constants.ManageCaseRoleConstants;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

@Slf4j
public final class DocumentUtil {

    private static final List<String> HIDDEN_DOCUMENT_TYPES_FOR_CLAIMANT = List.of(
        DocumentConstants.ET1_VETTING,
        DocumentConstants.ET3_PROCESSING,
        DocumentConstants.REFERRAL_JUDICIAL_DIRECTION,
        DocumentConstants.APP_FOR_A_WITNESS_ORDER_R,
        DocumentConstants.CONTACT_THE_TRIBUNAL_R,
        DocumentConstants.COT3,
        DocumentConstants.TRIBUNAL_CASE_FILE,
        DocumentConstants.INITIAL_CONSIDERATION,
        DocumentConstants.OTHER,
        DocumentConstants.NEEDS_UPDATING
    );

    private static final List<String> RESPONDENT_APPLICATION_DOC_TYPE = List.of(
        DocumentConstants.APP_TO_AMEND_RESPONSE,
        DocumentConstants.CHANGE_OF_PARTYS_DETAILS,
        DocumentConstants.C_HAS_NOT_COMPLIED_WITH_AN_ORDER_R,
        DocumentConstants.APP_TO_HAVE_A_LEGAL_OFFICER_DECISION_CONSIDERED_AFRESH_R,
        DocumentConstants.CONTACT_THE_TRIBUNAL_R,
        DocumentConstants.APP_TO_ORDER_THE_C_TO_DO_SOMETHING,
        DocumentConstants.APP_FOR_A_WITNESS_ORDER_R,
        DocumentConstants.APP_TO_POSTPONE_R,
        DocumentConstants.APP_FOR_A_JUDGMENT_TO_BE_RECONSIDERED_R,
        DocumentConstants.APP_TO_RESTRICT_PUBLICITY_R,
        DocumentConstants.APP_TO_STRIKE_OUT_ALL_OR_PART_OF_THE_CLAIM,
        DocumentConstants.APP_TO_VARY_OR_REVOKE_AN_ORDER_R
    );

    private static final List<String> CLAIMANT_APPLICATION_DOC_TYPE = List.of(
        DocumentConstants.WITHDRAWAL_OF_ALL_OR_PART_CLAIM,
        DocumentConstants.CHANGE_OF_PARTYS_DETAILS,
        DocumentConstants.APP_TO_POSTPONE_C,
        DocumentConstants.APP_TO_VARY_OR_REVOKE_AN_ORDER_C,
        DocumentConstants.APP_TO_HAVE_A_LEGAL_OFFICER_DECISION_CONSIDERED_AFRESH_C,
        DocumentConstants.APP_TO_AMEND_CLAIM,
        DocumentConstants.APP_TO_ORDER_THE_R_TO_DO_SOMETHING,
        DocumentConstants.APP_FOR_A_WITNESS_ORDER_C,
        DocumentConstants.R_HAS_NOT_COMPLIED_WITH_AN_ORDER_C,
        DocumentConstants.APP_TO_RESTRICT_PUBLICITY_C,
        DocumentConstants.APP_TO_STRIKE_OUT_ALL_OR_PART_OF_THE_RESPONSE,
        DocumentConstants.APP_FOR_A_JUDGMENT_TO_BE_RECONSIDERED_C,
        DocumentConstants.CONTACT_THE_TRIBUNAL_C
    );

    private static final List<String> HIDDEN_DOCUMENT_TYPES_FOR_RESPONDENT = List.of(
        DocumentConstants.ET1_VETTING,
        DocumentConstants.ET3_PROCESSING,
        DocumentConstants.REJECTION_OF_CLAIM,
        DocumentConstants.REFERRAL_JUDICIAL_DIRECTION,
        DocumentConstants.APP_FOR_A_WITNESS_ORDER_C,
        DocumentConstants.CONTACT_THE_TRIBUNAL_C,
        DocumentConstants.COT3,
        DocumentConstants.TRIBUNAL_CASE_FILE,
        DocumentConstants.INITIAL_CONSIDERATION,
        DocumentConstants.OTHER,
        DocumentConstants.NEEDS_UPDATING
    );

    private DocumentUtil() {
        // Utility classes should not have a public or default constructor.
    }

    public static void filterCasesDocumentsByCaseUserRole(List<CaseDetails> caseDetailsList, String caseUserRole) {
        for (CaseDetails caseDetails : caseDetailsList) {
            String caseId;
            if (ObjectUtils.isNotEmpty(caseDetails.getData())
                && ObjectUtils.isNotEmpty(caseDetails.getData().get("ethosCaseReference"))) {
                caseId = caseDetails.getData().get("ethosCaseReference").toString();
            } else {
                caseId = ObjectUtils.isNotEmpty(caseDetails.getId()) ? caseDetails.getId().toString() : "";
            }
            filterCaseDocumentsByCaseUserRole(caseDetails, caseId, caseUserRole);
        }
    }

    private static void filterCaseDocumentsByCaseUserRole(CaseDetails caseDetails, String caseId, String caseUserRole) {
        List<LinkedHashMap<String, Object>> documentCollection = getCaseDocumentCollectionFromCaseDetails(caseDetails);
        if (CollectionUtils.isNotEmpty(documentCollection)) {
            for (Iterator<LinkedHashMap<String, Object>> iterator = documentCollection.iterator();
                 iterator.hasNext();) {
                removeHiddenDocumentFromCollectionByCaseUserRole(iterator, caseId, caseUserRole);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static  List<LinkedHashMap<String, Object>> getCaseDocumentCollectionFromCaseDetails(
        CaseDetails caseDetails) {
        if (ObjectUtils.isNotEmpty(caseDetails.getData())
            && ObjectUtils.isNotEmpty(caseDetails.getData().get("documentCollection"))) {
            return (List<LinkedHashMap<String, Object>>) caseDetails.getData().get("documentCollection");
        }
        return null;
    }

    private static void removeHiddenDocumentFromCollectionByCaseUserRole(
        Iterator<LinkedHashMap<String, Object>> iterator, String caseId, String caseUserRole) {
        LinkedHashMap<String, Object> documentInfo = iterator.next();
        if (ObjectUtils.isNotEmpty(documentInfo.get("value"))) {
            try {
                DocumentType documentType = GenericServiceUtil
                    .mapJavaObjectToClass(DocumentType.class, documentInfo.get("value"));
                if (isDocumentHiddenForCaseUserRole(documentType, caseUserRole)) {
                    iterator.remove();
                }
            } catch (JsonProcessingException jpe) {
                GenericServiceUtil.logException(
                    "Exception occurred while processing documents (JSON PARSE PROBLEM)", caseId,
                    jpe.getMessage(), "filterClaimantDocuments", "DocumentUtil");
            }
        }
    }

    private static boolean isDocumentHiddenForCaseUserRole(DocumentType documentType, String caseUserRole) {
        return isHiddenDocumentTypeForCaseUserRole(documentType.getTypeOfDocument(), caseUserRole)
            || isHiddenDocumentTypeForCaseUserRole(documentType.getDocumentType(), caseUserRole);
    }

    private static boolean isHiddenDocumentTypeForCaseUserRole(String documentType, String caseUserRole) {
        if (StringUtils.isNotBlank(documentType)) {
            String lowerCaseDocumentType = documentType.toLowerCase(Locale.UK).trim();
            List<String> mergedList = getMergedListByCaseUserRole(caseUserRole);
            return mergedList.stream()
                .anyMatch(type -> type.equalsIgnoreCase(lowerCaseDocumentType));
        }
        return false;
    }

    private static List<String> getMergedListByCaseUserRole(String caseUserRole) {
        if (ManageCaseRoleConstants.CASE_USER_ROLE_CREATOR.equals(caseUserRole)) {
            return Stream.of(
                    HIDDEN_DOCUMENT_TYPES_FOR_CLAIMANT,
                    RESPONDENT_APPLICATION_DOC_TYPE,
                    CLAIMANT_APPLICATION_DOC_TYPE
                )
                .flatMap(List::stream)
                .distinct()
                .toList();
        }
        if (ManageCaseRoleConstants.CASE_USER_ROLE_DEFENDANT.equals(caseUserRole)) {
            return Stream.of(
                    HIDDEN_DOCUMENT_TYPES_FOR_RESPONDENT,
                    RESPONDENT_APPLICATION_DOC_TYPE,
                    CLAIMANT_APPLICATION_DOC_TYPE
                )
                .flatMap(List::stream)
                .distinct()
                .toList();
        }
        // If case role not [DEFENDANT] or [CREATOR]
        return Stream.of(
                List.of(StringUtils.EMPTY)
            )
            .flatMap(List::stream)
            .distinct()
            .toList();
    }

}
