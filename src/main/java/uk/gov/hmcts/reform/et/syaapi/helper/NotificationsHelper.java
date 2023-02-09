package uk.gov.hmcts.reform.et.syaapi.helper;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.DateListedTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.HearingTypeItem;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.AbstractMap;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.google.common.base.Strings.isNullOrEmpty;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.HEARING_STATUS_LISTED;

@Slf4j
@SuppressWarnings({"PMD.SimpleDateFormatNeedsLocale", "PMD.UseConcurrentHashMap"})
public final class NotificationsHelper {

    public static final Map<String, String> SHORT_TEXT_MAP = Map.ofEntries(
        new AbstractMap.SimpleEntry<>("withdraw", "Withdraw all/part of claim"),
        new AbstractMap.SimpleEntry<>("change-details", "Change my personal details"),
        new AbstractMap.SimpleEntry<>("postpone", "Postpone a hearing"),
        new AbstractMap.SimpleEntry<>("vary", "Vary/revoke an order"),
        new AbstractMap.SimpleEntry<>("reconsider-decision", "Consider a decision afresh"),
        new AbstractMap.SimpleEntry<>("amend", "Amend my claim"),
        new AbstractMap.SimpleEntry<>("respondent", "Order respondent to do something"),
        new AbstractMap.SimpleEntry<>("witness", "Order a witness to attend"),
        new AbstractMap.SimpleEntry<>("non-compliance", "Tell tribunal respondent not complied"),
        new AbstractMap.SimpleEntry<>("publicity", "Restrict publicity"),
        new AbstractMap.SimpleEntry<>("strike", "Strike out all/part of response"),
        new AbstractMap.SimpleEntry<>("reconsider-judgement", "Reconsider judgement"),
        new AbstractMap.SimpleEntry<>("other", "Contact about something else")
    );

    private NotificationsHelper() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * Format all respondent names into one string.
     *
     * @param caseData existing case data
     * @return respondent names
     */
    public static String getRespondentNames(CaseData caseData) {
        return caseData.getRespondentCollection().stream()
            .map(o -> o.getValue().getRespondentName())
            .collect(Collectors.joining(", "));
    }

    /**
     * Gets the .
     *
     * @param caseData existing case data next future hearing date
     * @return hearing date
     */
    public static String getNearestHearingToReferral(CaseData caseData, String defaultValue) {
        String earliestFutureHearingDate = getEarliestFutureHearingDate(caseData.getHearingCollection());

        if (earliestFutureHearingDate == null) {
            return defaultValue;
        }

        try {
            Date hearingStartDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS").parse(earliestFutureHearingDate);
            return new SimpleDateFormat("dd MMM yyyy").format(hearingStartDate);
        } catch (ParseException e) {
            log.info("Failed to parse hearing date when creating new referral");
            return defaultValue;
        }
    }

    private static String getEarliestFutureHearingDate(List<HearingTypeItem> hearingCollection) {
        if (CollectionUtils.isEmpty(hearingCollection)) {
            return null;
        }

        List<DateListedTypeItem> earliestDatePerHearing = hearingCollection.stream()
            .map(NotificationsHelper::mapEarliest)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());

        if (earliestDatePerHearing.isEmpty()) {
            return null;
        }

        return Collections.min(earliestDatePerHearing, Comparator.comparing(c -> c.getValue().getListedDate()))
            .getValue().getListedDate();
    }

    private static DateListedTypeItem mapEarliest(HearingTypeItem hearingTypeItem) {
        List<DateListedTypeItem> futureHearings = filterFutureHearings(hearingTypeItem.getValue()
                                                                           .getHearingDateCollection());
        if (futureHearings.isEmpty()) {
            return null;
        }
        return Collections.min(futureHearings, Comparator.comparing(c -> c.getValue().getListedDate()));
    }

    private static List<DateListedTypeItem> filterFutureHearings(List<DateListedTypeItem> hearingDateCollection) {
        return hearingDateCollection.stream()
            .filter(d -> isDateInFuture(d.getValue().getListedDate(), LocalDateTime.now())
                && HEARING_STATUS_LISTED.equals(d.getValue().getHearingStatus()))
            .collect(Collectors.toList());
    }

    private static boolean isDateInFuture(String date, LocalDateTime now) {
        //Azure times are always in UTC and users enter Europe/London Times,
        // so respective zonedDateTimes should be compared.
        return !isNullOrEmpty(date) && LocalDateTime.parse(date).atZone(ZoneId.of("Europe/London"))
            .isAfter(now.atZone(ZoneId.of("UTC")));
    }
}
