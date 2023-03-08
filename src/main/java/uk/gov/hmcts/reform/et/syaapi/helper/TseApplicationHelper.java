package uk.gov.hmcts.reform.et.syaapi.helper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTseApplicationType;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTseApplicationTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.TseRespondTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.TseRespondType;
import uk.gov.hmcts.reform.et.syaapi.models.RespondToApplicationRequest;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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

    public static GenericTseApplicationTypeItem getSelectedApplication(
        RespondToApplicationRequest request,
        List<GenericTseApplicationTypeItem> applications) {
        return applications.stream()
            .filter(a -> a.getId().equals(request.getApplicationId()))
            .findAny()
            .orElse(null);
    }

    public static void setRespondentApplicationWithResponse(RespondToApplicationRequest request,
                                                            GenericTseApplicationType appToModify) {
        if (CollectionUtils.isEmpty(appToModify.getRespondCollection())) {
            appToModify.setRespondCollection(new ArrayList<>());
        }
        TseRespondType responseToAdd = request.getResponse();
        responseToAdd.setDate(TseApplicationHelper.formatCurrentDate(LocalDate.now()));
        responseToAdd.setFrom(CLAIMANT);

        appToModify.getRespondCollection().add(TseRespondTypeItem.builder()
                                                   .id(UUID.randomUUID().toString())
                                                   .value(responseToAdd).build());
        appToModify.setResponsesCount(
            String.valueOf(appToModify.getRespondCollection().size()));
        appToModify.setApplicationState(IN_PROGRESS);
    }
}
