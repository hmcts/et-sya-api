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
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.google.common.base.Strings.isNullOrEmpty;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.HEARING_STATUS_LISTED;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.SEND_EMAIL_PARAMS_CASE_ID;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.SEND_EMAIL_PARAMS_CASE_NUMBER_KEY;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.SEND_EMAIL_PARAMS_SHORTTEXT_KEY;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.SEND_EMAIL_PARAMS_SUBJECTLINE_KEY;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@SuppressWarnings({"PMD.SimpleDateFormatNeedsLocale",
    "PMD.UseConcurrentHashMap",
    "PMD.TooManyMethods",
    "checkstyle:HideUtilityClassConstructor"})
public final class NotificationsHelper {

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

    public static void addCommonParameters(Map<String, Object> parameters, String claimant, String respondentNames,
                                            String caseId, String caseNumber) {
        parameters.put("claimant", claimant);
        parameters.put("respondentNames", respondentNames);
        parameters.put(SEND_EMAIL_PARAMS_CASE_ID, caseId);
        parameters.put(SEND_EMAIL_PARAMS_CASE_NUMBER_KEY, caseNumber);
    }

    public static void addCommonParameters(Map<String, Object> parameters, String claimant, String respondentNames,
                                            String caseId, String caseNumber, String subjectLine) {
        addCommonParameters(parameters, claimant, respondentNames, caseId, caseNumber);
        parameters.put(SEND_EMAIL_PARAMS_SUBJECTLINE_KEY, subjectLine);
    }

    public static void addCommonParameters(Map<String, Object> parameters, String claimant, String respondentNames,
                                            String caseId, String caseNumber, String subjectLine, String shortText) {
        addCommonParameters(parameters, claimant, respondentNames, caseId, caseNumber, subjectLine);
        parameters.put(SEND_EMAIL_PARAMS_SHORTTEXT_KEY, shortText);
    }

    public static void addCommonParameters(Map<String, Object> parameters, CaseData caseData, String caseId) {
        String claimant = String.join(" ",
                                      caseData.getClaimantIndType().getClaimantFirstNames(),
                                      caseData.getClaimantIndType().getClaimantLastName()
        );
        String caseNumber = caseData.getEthosCaseReference();
        String respondentNames = getRespondentNames(caseData);

        addCommonParameters(parameters, claimant, respondentNames, caseId, caseNumber, caseNumber);
    }
}
