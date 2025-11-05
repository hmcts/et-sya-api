package uk.gov.hmcts.reform.et.syaapi.helper;

import uk.gov.hmcts.et.common.model.ccd.items.PseResponseTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.PseStatusTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.PseResponseType;
import uk.gov.hmcts.et.common.model.ccd.types.PseStatusType;
import uk.gov.hmcts.et.common.model.ccd.types.SendNotificationTypeItem;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public final class PseNotificationHelper {
    private PseNotificationHelper() {
    }

    /**
     * Finds the notification by ID.
     * @param notifications  - list of all notifications attached to the case
     * @param notificationId - id of notification we're trying to find
     * @return the {@link SendNotificationTypeItem} to be updated
     */
    public static SendNotificationTypeItem getSelectedNotification(
        List<SendNotificationTypeItem> notifications,
        String notificationId) {
        return notifications.stream()
            .filter(a -> a.getId().equals(notificationId))
            .findAny()
            .orElse(null);
    }

    /**
     * Build new PseStatusTypeItem with user idam id and state.
     * @param userIdamId user idam id
     * @param state state to update
     * @return PseStatusTypeItem
     */
    public static PseStatusTypeItem buildPseStatusTypeItem(String userIdamId, String state) {
        return PseStatusTypeItem.builder()
            .id(UUID.randomUUID().toString())
            .value(PseStatusType.builder()
                       .userIdamId(userIdamId)
                       .notificationState(state)
                       .dateTime(LocalDateTime.now().toString())
                       .build())
            .build();
    }

    /**
     * Build new PseResponseTypeItem with given response type.
     * @param pseResponseType PseStatusTypeItem
     * @return PseResponseTypeItem
     */
    public static PseResponseTypeItem getPseResponseTypeItem(PseResponseType pseResponseType) {
        return PseResponseTypeItem.builder()
                .id(UUID.randomUUID().toString())
                .value(pseResponseType)
                .build();
    }
}
