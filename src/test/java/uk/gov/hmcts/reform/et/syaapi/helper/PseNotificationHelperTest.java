package uk.gov.hmcts.reform.et.syaapi.helper;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.et.common.model.ccd.items.PseStatusTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.PseStatusType;
import uk.gov.hmcts.et.common.model.ccd.types.SendNotificationTypeItem;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class PseNotificationHelperTest {
    DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    @Test
    void getSelectedNotification_returnsMatchingNotification() {
        SendNotificationTypeItem item1 = SendNotificationTypeItem.builder().id("id-1").build();
        SendNotificationTypeItem item2 = SendNotificationTypeItem.builder().id("id-2").build();
        List<SendNotificationTypeItem> notifications = List.of(item1, item2);

        SendNotificationTypeItem result = PseNotificationHelper.getSelectedNotification(notifications, "id-2");

        assertThat(result).isEqualTo(item2);
    }

    @Test
    void getSelectedNotification_returnsNullIfNotFound() {
        SendNotificationTypeItem item = SendNotificationTypeItem.builder().id("id-1").build();
        List<SendNotificationTypeItem> notifications = List.of(item);

        SendNotificationTypeItem result = PseNotificationHelper.getSelectedNotification(notifications, "not-found");

        assertThat(result).isNull();
    }

    @Test
    void buildPseStatusTypeItem_buildsItemWithCorrectFields() {
        String userIdamId = "user-123";
        String state = "notViewedYet";

        PseStatusTypeItem item = PseNotificationHelper.buildPseStatusTypeItem(userIdamId, state);

        assertThat(item.getId()).isNotNull();
        PseStatusType value = item.getValue();
        assertThat(value.getUserIdamId()).isEqualTo(userIdamId);
        assertThat(value.getNotificationState()).isEqualTo(state);
        assertDoesNotThrow(() -> LocalDateTime.parse(value.getDateTime(), formatter));
    }
}
