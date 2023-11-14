package uk.gov.hmcts.reform.et.syaapi.helper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.DateListedTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.HearingTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.RepresentedTypeRItem;
import uk.gov.hmcts.et.common.model.ccd.types.RepresentedTypeR;
import uk.gov.hmcts.et.common.model.ccd.types.RespondentSumType;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.google.common.base.Strings.isNullOrEmpty;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.HEARING_STATUS_LISTED;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@SuppressWarnings({"checkstyle:HideUtilityClassConstructor"})
public final class NotificationsHelper {

    public static final String INVALID_DATE = "Invalid date";

    /**
     * Format all respondent names into one string.
     *
     * @param caseData existing case data
     * @return respondent names
     */
    public static String getRespondentNames(CaseData caseData) {
        if (CollectionUtils.isEmpty(caseData.getRespondentCollection())) {
            return StringUtils.EMPTY;
        }
        return caseData.getRespondentCollection().stream()
            .map(o -> o.getValue().getRespondentName())
            .collect(Collectors.joining(", "));
    }

    public static String getEmailAddressForRespondent(CaseData caseData, RespondentSumType respondent) {
        RepresentedTypeR representative = getRespondentRepresentative(caseData, respondent);
        if (representative != null) {
            String repEmail = representative.getRepresentativeEmailAddress();
            if (!isNullOrEmpty(repEmail)) {
                return repEmail;
            }
        }

        return isNullOrEmpty(respondent.getRespondentEmail()) ? "" : respondent.getRespondentEmail();
    }

    private static RepresentedTypeR getRespondentRepresentative(CaseData caseData, RespondentSumType respondent) {
        List<RepresentedTypeRItem> repCollection = caseData.getRepCollection();

        if (CollectionUtils.isEmpty(repCollection)) {
            return null;
        }

        Optional<RepresentedTypeRItem> respondentRep = repCollection.stream()
            .filter(o -> respondent.getRespondentName().equals(o.getValue().getRespRepName()))
            .findFirst();

        return respondentRep.map(RepresentedTypeRItem::getValue).orElse(null);
    }

    /**
     * Gets the nearest future hearing date.
     *
     * @param caseData     existing case data next future hearing date
     * @param defaultValue default value if hearing with future date is not found
     * @return hearing date
     */
    public static String getNearestHearingToReferral(CaseData caseData, String defaultValue) {
        String earliestFutureHearingDate = getEarliestFutureHearingDate(caseData.getHearingCollection());

        if (earliestFutureHearingDate == null) {
            return defaultValue;
        }

        return formatToSimpleDate(defaultValue, earliestFutureHearingDate);
    }

    /**
     * Searches hearing collection by id and gets the nearest future hearing date.
     *
     * @param hearingCollection all hearings on case data
     * @param hearingId         id of hearing we are searching
     * @return hearing date
     */
    public static String getEarliestDateForHearing(List<HearingTypeItem> hearingCollection,
                                                   String hearingId) {
        Optional<HearingTypeItem> hearing = hearingCollection.stream().filter(x -> Objects.equals(
            x.getId(),
            hearingId
        )).findFirst();

        if (hearing.isPresent()) {
            DateListedTypeItem earliestFutureDate = mapEarliest(hearing.get());
            if (earliestFutureDate != null) {
                return formatToSimpleDate(INVALID_DATE, earliestFutureDate.getValue().getListedDate());
            }
            throw new IllegalArgumentException("Hearing does not have any future dates");
        }
        throw new IllegalArgumentException("Hearing does not exist in hearing collection");
    }

    private static String formatToSimpleDate(String defaultValue, String earliestFutureHearingDate) {
        try {
            Date hearingStartDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS").parse(earliestFutureHearingDate);
            return new SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH).format(hearingStartDate);
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
            .toList();

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
            .toList();
    }

    private static boolean isDateInFuture(String date, LocalDateTime now) {
        //Azure times are always in UTC and users enter Europe/London Times,
        // so respective zonedDateTimes should be compared.
        return !isNullOrEmpty(date) && LocalDateTime.parse(date).atZone(ZoneId.of("Europe/London"))
            .isAfter(now.atZone(ZoneId.of("UTC")));
    }
}
