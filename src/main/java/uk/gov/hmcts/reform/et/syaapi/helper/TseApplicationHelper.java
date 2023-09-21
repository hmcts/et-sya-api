package uk.gov.hmcts.reform.et.syaapi.helper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.DocumentTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTseApplicationType;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTseApplicationTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.TseAdminRecordDecisionTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.TseRespondTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.TseRespondType;
import uk.gov.hmcts.et.common.model.ccd.types.citizenhub.ClaimantTse;
import uk.gov.hmcts.reform.et.syaapi.models.RespondToApplicationRequest;
import uk.gov.hmcts.reform.et.syaapi.service.CaseDocumentService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static uk.gov.hmcts.ecm.common.model.helper.Constants.CLAIMANT_TITLE;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.CLAIMANT_CORRESPONDENCE_DOCUMENT;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@SuppressWarnings({"checkstyle:HideUtilityClassConstructor"})
public final class TseApplicationHelper {

    public static final DateTimeFormatter NEW_DATE_PATTERN = DateTimeFormatter.ofPattern("d MMMM yyyy");
    public static final String CLAIMANT = "Claimant";
    public static final String WAITING_FOR_TRIBUNAL = "waitingForTheTribunal";

    public static String formatCurrentDate(LocalDate date) {
        return date.format(NEW_DATE_PATTERN);
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
    public static void setRespondentApplicationWithResponse(RespondToApplicationRequest request,
                                                            GenericTseApplicationType appToModify,
                                                            CaseData caseData,
                                                            CaseDocumentService caseDocumentService) {
        if (CollectionUtils.isEmpty(appToModify.getRespondCollection())) {
            appToModify.setRespondCollection(new ArrayList<>());
        }
        TseRespondType responseToAdd = request.getResponse();
        responseToAdd.setDate(TseApplicationHelper.formatCurrentDate(LocalDate.now()));
        responseToAdd.setFrom(CLAIMANT);

        if (request.getSupportingMaterialFile() != null) {
            DocumentTypeItem documentTypeItem = caseDocumentService.createDocumentTypeItem(
                CLAIMANT_CORRESPONDENCE_DOCUMENT,
                request.getSupportingMaterialFile()
            );

            responseToAdd.setSupportingMaterial(new ArrayList<>());
            responseToAdd.getSupportingMaterial().add(documentTypeItem);

            String applicationDoc = getApplicationDoc(appToModify);
            String docName = "Application %s - %s - Attachment.pdf".formatted(
                appToModify.getNumber(),
                appToModify.getType()
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
        } else {
            return uk.gov.hmcts.ecm.common.helpers.DocumentHelper.respondentApplicationToDocType(
                applicationType.getType());
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
}
