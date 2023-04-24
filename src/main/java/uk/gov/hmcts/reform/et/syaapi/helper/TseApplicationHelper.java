package uk.gov.hmcts.reform.et.syaapi.helper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.DocumentTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTseApplicationType;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTseApplicationTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.TseRespondTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.TseRespondType;
import uk.gov.hmcts.reform.et.syaapi.models.RespondToApplicationRequest;
import uk.gov.hmcts.reform.et.syaapi.service.CaseDocumentService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.CLAIMANT_CORRESPONDENCE_DOCUMENT;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@SuppressWarnings({"checkstyle:HideUtilityClassConstructor"})
public final class TseApplicationHelper {

    public static final DateTimeFormatter NEW_DATE_PATTERN = DateTimeFormatter.ofPattern("d MMMM yyyy");
    public static final String CLAIMANT = "Claimant";
    public static final String IN_PROGRESS = "inProgress";

    public static String formatCurrentDate(LocalDate date) {
        return date.format(NEW_DATE_PATTERN);
    }

    /**
     * Finds the application by ID.
     *
     * @param request - request from the claimant
     * @param applications - list of all applications attached to the case
     * @return the {@link GenericTseApplicationTypeItem} to be updated
     */
    public static GenericTseApplicationTypeItem getSelectedApplication(
        RespondToApplicationRequest request,
        List<GenericTseApplicationTypeItem> applications) {
        return applications.stream()
            .filter(a -> a.getId().equals(request.getApplicationId()))
            .findAny()
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

            caseData.getDocumentCollection().add(documentTypeItem);

            responseToAdd.setSupportingMaterial(new ArrayList<>());
            responseToAdd.getSupportingMaterial().add(documentTypeItem);
        }

        appToModify.getRespondCollection().add(TseRespondTypeItem.builder()
                                                   .id(UUID.randomUUID().toString())
                                                   .value(responseToAdd).build());
        appToModify.setResponsesCount(
            String.valueOf(appToModify.getRespondCollection().size()));
        appToModify.setApplicationState(IN_PROGRESS);
    }
}
