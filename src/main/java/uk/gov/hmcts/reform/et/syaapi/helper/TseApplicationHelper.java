package uk.gov.hmcts.reform.et.syaapi.helper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FilenameUtils;
import uk.gov.hmcts.ecm.common.model.helper.Constants;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.DocumentTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTseApplicationType;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTseApplicationTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.TseAdminRecordDecisionTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.TseRespondTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.TseStatusTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.TseRespondType;
import uk.gov.hmcts.et.common.model.ccd.types.citizenhub.ClaimantTse;
import uk.gov.hmcts.reform.et.syaapi.models.RespondToApplicationRequest;
import uk.gov.hmcts.reform.et.syaapi.service.CaseDocumentService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static uk.gov.hmcts.ecm.common.model.helper.Constants.CLAIMANT_TITLE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.OLD_DATE_TIME_PATTERN;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.UPDATED;
import static uk.gov.hmcts.ecm.common.model.helper.DocumentConstants.CLAIMANT_CORRESPONDENCE;
import static uk.gov.hmcts.ecm.common.model.helper.DocumentConstants.RESPONDENT_CORRESPONDENCE;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.UK_LOCAL_DATE_PATTERN;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyrConstants.CLAIMANT_TSE_AMEND_CLAIM;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyrConstants.CLAIMANT_TSE_CHANGE_PERSONAL_DETAILS;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyrConstants.CLAIMANT_TSE_CONSIDER_DECISION_AFRESH;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyrConstants.CLAIMANT_TSE_CONTACT_THE_TRIBUNAL;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyrConstants.CLAIMANT_TSE_ORDER_A_WITNESS_TO_ATTEND;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyrConstants.CLAIMANT_TSE_ORDER_OTHER_PARTY;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyrConstants.CLAIMANT_TSE_POSTPONE_A_HEARING;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyrConstants.CLAIMANT_TSE_RECONSIDER_JUDGMENT;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyrConstants.CLAIMANT_TSE_RESPONDENT_NOT_COMPLIED;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyrConstants.CLAIMANT_TSE_RESTRICT_PUBLICITY;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyrConstants.CLAIMANT_TSE_STRIKE_OUT_ALL_OR_PART;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyrConstants.CLAIMANT_TSE_VARY_OR_REVOKE_AN_ORDER;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyrConstants.CLAIMANT_TSE_WITHDRAW_CLAIM;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@SuppressWarnings({"checkstyle:HideUtilityClassConstructor"})
public final class TseApplicationHelper {

    public static final String WAITING_FOR_TRIBUNAL = "waitingForTheTribunal";
    private static final Map<String, String> APPLICATION_TYPE_MAP = new ConcurrentHashMap<>();

    static {
        APPLICATION_TYPE_MAP.put(CLAIMANT_TSE_AMEND_CLAIM, "amend");
        APPLICATION_TYPE_MAP.put(CLAIMANT_TSE_CHANGE_PERSONAL_DETAILS, "change-details");
        APPLICATION_TYPE_MAP.put(CLAIMANT_TSE_CONSIDER_DECISION_AFRESH, "reconsider-decision");
        APPLICATION_TYPE_MAP.put(CLAIMANT_TSE_ORDER_A_WITNESS_TO_ATTEND, "witness");
        APPLICATION_TYPE_MAP.put(CLAIMANT_TSE_ORDER_OTHER_PARTY, "respondent");
        APPLICATION_TYPE_MAP.put(CLAIMANT_TSE_POSTPONE_A_HEARING, "postpone");
        APPLICATION_TYPE_MAP.put(CLAIMANT_TSE_RECONSIDER_JUDGMENT, "reconsider-judgement");
        APPLICATION_TYPE_MAP.put(CLAIMANT_TSE_RESPONDENT_NOT_COMPLIED, "non-compliance");
        APPLICATION_TYPE_MAP.put(CLAIMANT_TSE_RESTRICT_PUBLICITY, "publicity");
        APPLICATION_TYPE_MAP.put(CLAIMANT_TSE_STRIKE_OUT_ALL_OR_PART, "strike");
        APPLICATION_TYPE_MAP.put(CLAIMANT_TSE_VARY_OR_REVOKE_AN_ORDER, "vary");
        APPLICATION_TYPE_MAP.put(CLAIMANT_TSE_WITHDRAW_CLAIM, "withdraw");
        APPLICATION_TYPE_MAP.put(CLAIMANT_TSE_CONTACT_THE_TRIBUNAL, "other");
    }

    /**
     * Gives current date in string format.
     * @return current date in "dd MMM yyyy" format
     */
    public static String formatCurrentDate(LocalDate date) {
        return date.format(UK_LOCAL_DATE_PATTERN);
    }

    /**
     * Gives current datetime in string format.
     * @return current datetime in "yyyy-MM-dd'T'HH:mm:ss.SSS" format
     */
    public static String getCurrentDateTime() {
        return LocalDateTime.now().format(OLD_DATE_TIME_PATTERN);
    }

    /**
     * Finds the application by ID.
     *
     * @param applications - list of all applications attached to the case
     * @param applicationId - id of application we're trying to find
     * @return the {@link GenericTseApplicationTypeItem} to be updated
     */
    public static GenericTseApplicationTypeItem getSelectedApplication(
        List<GenericTseApplicationTypeItem> applications,
        String applicationId) {
        return applications.stream()
            .filter(a -> a.getId().equals(applicationId))
            .findAny()
            .orElse(null);
    }

    /**
     * Finds the response by ID.
     *
     * @param responds - list of all applications attached to the case
     * @param respondId - id of application we're trying to find
     * @return the {@link GenericTseApplicationTypeItem} to be updated
     */
    public static TseRespondTypeItem getResponseInSelectedApplication(
        List<TseRespondTypeItem> responds,
        String respondId) {
        return responds.stream()
            .filter(a -> a.getId().equals(respondId))
            .findAny()
            .orElse(null);
    }

    /**
     * Finds the admin decision by ID.
     *
     * @param selectedApplication - application to update
     * @param adminDecisionId - id of decision we're trying to find
     * @return the {@link TseAdminRecordDecisionTypeItem} to be updated
     */
    public static TseAdminRecordDecisionTypeItem findAdminDecision(GenericTseApplicationTypeItem selectedApplication,
                                                          String adminDecisionId) {
        return selectedApplication.getValue().getAdminDecision().stream()
            .filter(a -> a.getId().equals(adminDecisionId))
            .findFirst()
            .orElse(null);
    }

    /**
     * Finds the response by ID.
     *
     * @param selectedApplication - application to update
     * @param responseId - id of decision we're trying to find
     * @return the {@link TseRespondTypeItem} to be updated
     */
    public static TseRespondTypeItem findResponse(GenericTseApplicationTypeItem selectedApplication,
                                                  String responseId) {
        return selectedApplication.getValue().getRespondCollection().stream()
            .filter(a -> a.getId().equals(responseId))
            .findFirst()
            .orElse(null);
    }

    /**
     * Sets case data with claimant's response to the respondent.
     *
     * @param request - request from the claimant
     * @param appToModify - selected respondent application to respond to
     * @param caseData - case data
     * @param caseDocumentService - case document service to create pdf of response
     */
    public static void setApplicationWithResponse(RespondToApplicationRequest request,
                                                  GenericTseApplicationType appToModify,
                                                  CaseData caseData,
                                                  CaseDocumentService caseDocumentService,
                                                  boolean isWorkAllocationEnabled,
                                                  String responseUserType) {
        if (CollectionUtils.isEmpty(appToModify.getRespondCollection())) {
            appToModify.setRespondCollection(new ArrayList<>());
        }
        TseRespondType responseToAdd = request.getResponse();
        responseToAdd.setDate(TseApplicationHelper.formatCurrentDate(LocalDate.now()));
        responseToAdd.setFrom(responseUserType);

        if (isWorkAllocationEnabled) {
            responseToAdd.setDateTime(getCurrentDateTime());
            responseToAdd.setApplicationType(appToModify.getType());
        }

        if (request.getSupportingMaterialFile() != null) {
            String documentType = CLAIMANT_TITLE.equals(responseUserType)
                ? CLAIMANT_CORRESPONDENCE
                : RESPONDENT_CORRESPONDENCE;

            DocumentTypeItem documentTypeItem = caseDocumentService.createDocumentTypeItem(
                documentType,
                request.getSupportingMaterialFile()
            );
            documentTypeItem.getValue().setShortDescription("Response to " + appToModify.getType());

            responseToAdd.setSupportingMaterial(new ArrayList<>());
            responseToAdd.getSupportingMaterial().add(documentTypeItem);

            String applicationDoc = getApplicationDoc(appToModify);
            String extension = FilenameUtils.getExtension(request.getSupportingMaterialFile().getDocumentFilename());
            String docName = "Application %s - %s - Attachment.%s".formatted(
                appToModify.getNumber(),
                appToModify.getType(),
                extension
            );
            request.getSupportingMaterialFile().setDocumentFilename(docName);
            documentTypeItem = caseDocumentService.createDocumentTypeItem(applicationDoc,
                                                                          request.getSupportingMaterialFile());

            if (caseData.getDocumentCollection() == null) {
                caseData.setDocumentCollection(new ArrayList<>());
            }
            caseData.getDocumentCollection().add(documentTypeItem);
        }

        appToModify.getRespondCollection().add(TseRespondTypeItem.builder()
                                                   .id(UUID.randomUUID().toString())
                                                   .value(responseToAdd).build());
        appToModify.setResponsesCount(
            String.valueOf(appToModify.getRespondCollection().size()));
        appToModify.setApplicationState(WAITING_FOR_TRIBUNAL);
        TseApplicationHelper.setRespondentApplicationState(appToModify, UPDATED);
    }

    /**
     * Gets the document type for the application.
     * @param applicationType - application to get document type for
     * @return the document type
     */
    public static String getApplicationDoc(GenericTseApplicationType applicationType) {
        if (CLAIMANT_TITLE.equals(applicationType.getApplicant())) {
            return uk.gov.hmcts.ecm.common.helpers.DocumentHelper.claimantApplicationTypeToDocType(
                getClaimantApplicationType(applicationType));
        } else if ("Claimant Representative".equals(applicationType.getApplicant())) {
            log.info("Claimant Representative application type: {}", applicationType.getType());
            String appType = claimantSelectApplicationToType(applicationType.getType());
            return uk.gov.hmcts.ecm.common.helpers.DocumentHelper.claimantApplicationTypeToDocType(appType);
        } else if (Constants.RESPONDENT_TITLE.equals(applicationType.getApplicant())
                   || "Respondent Representative".equals(applicationType.getApplicant())) {
            return uk.gov.hmcts.ecm.common.helpers.DocumentHelper.respondentApplicationToDocType(
                applicationType.getType());
        } else {
            throw new IllegalArgumentException("Invalid applicant type: " + applicationType.getApplicant());
        }
    }

    private static String claimantSelectApplicationToType(String selectApplication) {
        String type = APPLICATION_TYPE_MAP.get(selectApplication);
        if (type != null) {
            return type;
        } else {
            throw new IllegalArgumentException(String.format("Unexpected application type %s", selectApplication));
        }
    }

    private static String getClaimantApplicationType(GenericTseApplicationType applicationType) {
        return ClaimantTse.APP_TYPE_MAP.entrySet()
            .stream()
            .filter(entry -> entry.getValue().equals(applicationType.getType()))
            .map(Map.Entry::getKey)
            .findFirst()
            .orElse("");

    }

    /**
     * Update Respondent Application State list.
     * @param applicationType application in GenericTseApplicationType
     * @param newState new state
     */
    public static void setRespondentApplicationState(GenericTseApplicationType applicationType, String newState) {
        List<TseStatusTypeItem> respondentStateList = applicationType.getRespondentState();
        if (CollectionUtils.isEmpty(respondentStateList)) {
            return;
        }
        for (TseStatusTypeItem statusItem : respondentStateList) {
            if (statusItem != null && statusItem.getValue() != null) {
                statusItem.getValue().setApplicationState(newState);
            }
        }
    }
}
