package uk.gov.hmcts.reform.et.syaapi.helper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.DateListedTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.HearingTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.RepresentedTypeRItem;
import uk.gov.hmcts.et.common.model.ccd.types.PseResponseType;
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
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static com.google.common.base.Strings.isNullOrEmpty;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.HEARING_STATUS_LISTED;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.SEND_EMAIL_PARAMS_CASE_ID;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.SEND_EMAIL_PARAMS_CASE_NUMBER_KEY;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.SEND_EMAIL_PARAMS_CLAIMANT_TITLE;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.SEND_EMAIL_PARAMS_RESPONDENT_NAME;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.SEND_EMAIL_PARAMS_SHORTTEXT_KEY;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.SEND_EMAIL_PARAMS_SUBJECTLINE_KEY;
import static uk.gov.hmcts.reform.et.syaapi.helper.TseApplicationHelper.getCurrentDateTime;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@SuppressWarnings({"checkstyle:HideUtilityClassConstructor"})
public final class NotificationsHelper {

    private static final String INVALID_DATE = "Invalid date";
    private static final String EMPLOYER_CONTRACT_CLAIM = "Employer Contract Claim";
    public static final String MY_HMCTS = "MyHMCTS";

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

    /**
     * Retrieves a map of email addresses for a respondent and their representative.
     * The map contains email addresses as keys and a boolean value indicating whether
     * the email belongs to the respondent (true) or the representative (false).
     *
     * @param caseData  the case data containing respondent and representative information
     * @param respondent the respondent for whom the email addresses are being retrieved
     * @return a map where the keys are email addresses and the values are booleans indicating
     *         if the email belongs to the respondent (true) or the representative (false)
     */
    public static Map<String, Boolean> getRespondentAndRespRepEmailAddressesMap(CaseData caseData,
                                                                                RespondentSumType respondent) {
        Map<String, Boolean> emailAddressesMap = new ConcurrentHashMap<>();

        // get legal rep email if respondent is represented
        RepresentedTypeR representative = getRespondentRepresentative(caseData, respondent);
        if (representative != null && StringUtils.isNotBlank(representative.getRepresentativeEmailAddress())) {
            emailAddressesMap.put(representative.getRepresentativeEmailAddress(), false);
            return emailAddressesMap;
        }

        // get respondent email if respondent online
        if (respondent.getIdamId() != null) {
            String responseEmail = respondent.getResponseRespondentEmail();
            String respondentEmail = respondent.getRespondentEmail();

            if (StringUtils.isNotBlank(responseEmail)) {
                emailAddressesMap.put(responseEmail, true);
            } else if (StringUtils.isNotBlank(respondentEmail)) {
                emailAddressesMap.put(respondentEmail, true);
            }
        }

        return emailAddressesMap;
    }

    public static List<String> getEmailAddressesForRespondent(CaseData caseData, RespondentSumType respondent) {
        return getRespondentAndRespRepEmailAddressesMap(caseData, respondent).keySet().stream().toList();
    }

    public static RepresentedTypeR getRespondentRepresentative(CaseData caseData, RespondentSumType respondent) {
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

    /**
     * The ECC Event (UPDATE_NOTIFICATION_RESPONSE) can trigger Work Allocation tasks,
     * so we need to update some WA Enablers for the DMNs to read.
     *
     * @param isEccEnabled          is ECC Enabled
     * @param responseToUpdate      Claimant Response to update
     * @param notificationSubject   Notification Subjects
     */
    public static void updateWorkAllocationFields(boolean isEccEnabled,
                                                PseResponseType responseToUpdate,
                                                List<String> notificationSubject) {
        if (!isEccEnabled) {
            return;
        }

        responseToUpdate.setDateTime(getCurrentDateTime());

        if (!CollectionUtils.isEmpty(notificationSubject)
            && notificationSubject.contains(EMPLOYER_CONTRACT_CLAIM)) {
            responseToUpdate.setIsECC(YES);
        } else {
            responseToUpdate.setIsECC(NO);
        }
    }

    /**
     * Get the name of the current user(respondent).
     *
     * @param caseData      existing case data
     * @param applicantIdamId idam id of the applicant
     * @return respondent name
     */
    public static String getCurrentRespondentName(CaseData caseData, String applicantIdamId) {
        return caseData.getRespondentCollection().stream()
            .filter(r -> applicantIdamId.equals(r.getValue().getIdamId()))
            .map(r -> r.getValue().getRespondentName())
            .findFirst()
            .orElse(null);
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


    public static boolean isRepresentedClaimantWithMyHmctsCase(CaseData caseData) {
        return MY_HMCTS.equals(caseData.getCaseSource())
            && YES.equals(caseData.getClaimantRepresentedQuestion())
            && ObjectUtils.isNotEmpty(caseData.getRepresentativeClaimantType())
            && ObjectUtils.isNotEmpty(caseData.getRepresentativeClaimantType().getMyHmctsOrganisation());
    }

    /**
     * Adds common parameters to the parameters map.
     * @param parameters existing parameters map
     * @param claimant claimant name
     * @param respondentNames respondent names
     * @param caseId case id
     * @param caseNumber case number
     */
    public static void addCommonParameters(Map<String, Object> parameters, String claimant, String respondentNames,
                                           String caseId, String caseNumber) {
        parameters.put(SEND_EMAIL_PARAMS_CLAIMANT_TITLE, claimant);
        parameters.put(SEND_EMAIL_PARAMS_RESPONDENT_NAME, respondentNames);
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
        String claimant = String.join(
            " ",
            caseData.getClaimantIndType().getClaimantFirstNames(),
            caseData.getClaimantIndType().getClaimantLastName()
        );
        String caseNumber = caseData.getEthosCaseReference();
        String respondentNames = getRespondentNames(caseData);

        addCommonParameters(parameters, claimant, respondentNames, caseId, caseNumber, caseNumber);
    }
}
