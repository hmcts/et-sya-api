package uk.gov.hmcts.reform.et.syaapi.helper;

import org.apache.tika.utils.StringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.HearingTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.RepresentedTypeRItem;
import uk.gov.hmcts.et.common.model.ccd.types.Organisation;
import uk.gov.hmcts.et.common.model.ccd.types.RepresentedTypeC;
import uk.gov.hmcts.et.common.model.ccd.types.RepresentedTypeR;
import uk.gov.hmcts.reform.et.syaapi.model.CaseTestData;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.ET1;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.NOT_SET;
import static uk.gov.hmcts.reform.et.syaapi.helper.NotificationsHelper.MY_HMCTS;

class NotificationHelperTest {

    private final CaseTestData caseTestData;

    NotificationHelperTest() {
        caseTestData = new CaseTestData();
    }

    @Test
    void shouldReturnEmptyStringUtil() {
        // Given
        var data = caseTestData.getCaseDataWithClaimTypes().getCaseData();
        CaseData caseData = EmployeeObjectMapper.convertCaseDataMapToCaseDataObject(data);
        // When
        String respondentNames = NotificationsHelper.getRespondentNames(caseData);

        //Then
        assertThat(respondentNames).isEqualTo(StringUtils.EMPTY);
    }

    @Test
    void shouldReturnRepEmail() {
        // Given
        String repEmail = "rep@email.com";
        var rep = new RepresentedTypeR();
        rep.setId("11");
        rep.setRepresentativeEmailAddress(repEmail);
        rep.setRespRepName("Test Respondent Organisation -1-");
        RepresentedTypeRItem repItem = new RepresentedTypeRItem();
        repItem.setId("1");
        repItem.setValue(rep);
        List<RepresentedTypeRItem> itemList = new ArrayList<>();
        itemList.add(repItem);
        CaseData caseData = caseTestData.getCaseData();
        caseData.setRepCollection(itemList);

        // When
        List<String> emails = NotificationsHelper.getEmailAddressesForRespondent(
            caseData,
            caseData.getRespondentCollection().getFirst().getValue()
        );

        //Then
        assertTrue(emails.contains(repEmail));
    }

    @Test
    void shouldNotReturnRepEmail() {
        // Given
        var rep = new RepresentedTypeR();
        rep.setId("11");
        rep.setRespRepName("Test Respondent Organisation -1-");
        RepresentedTypeRItem repItem = new RepresentedTypeRItem();
        repItem.setId("1");
        repItem.setValue(rep);
        List<RepresentedTypeRItem> itemList = new ArrayList<>();
        itemList.add(repItem);

        CaseData caseData = caseTestData.getCaseData();
        caseData.getRespondentCollection().getFirst().getValue().setRespondentEmail(null);
        caseData.setRepCollection(itemList);

        // When
        List<String> emails = NotificationsHelper.getEmailAddressesForRespondent(
            caseData,
            caseData.getRespondentCollection().getFirst().getValue()
        );

        // Then
        assertTrue(emails.isEmpty());
    }

    @Test
    void shouldNotReturnRespondentEmail() {
        // Given
        CaseData caseData = caseTestData.getCaseData();
        caseData.getRespondentCollection().getFirst().getValue().setRespondentEmail(null);

        // When
        List<String> emails = NotificationsHelper.getEmailAddressesForRespondent(
            caseData,
            caseData.getRespondentCollection().getFirst().getValue()
        );

        // Then
        assertTrue(emails.isEmpty());
    }

    @Test
    void shouldGetNearestHearingToReferralHearingDateInPast() {
        // Given
        CaseData caseData = caseTestData.getCaseData();

        // When
        String hearingDate = NotificationsHelper.getNearestHearingToReferral(
            caseData,
            NOT_SET
        );

        // Then
        assertThat(hearingDate).isEqualTo(NOT_SET);
    }

    @Test
    void shouldGetNearestHearingToReferralHearingDateInFuture() throws ParseException {
        // Given
        CaseData caseData = caseTestData.getCaseData();

        String futureDate = LocalDateTime.now().plusDays(5).toString();
        caseData.getHearingCollection().getFirst().getValue()
            .getHearingDateCollection().getFirst().getValue().setListedDate(futureDate);

        Date hearingStartDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.ENGLISH).parse(futureDate);
        String simpleDate = new SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH).format(hearingStartDate);

        // When
        String hearingDate = NotificationsHelper.getNearestHearingToReferral(
            caseData,
            NOT_SET
        );

        // Then
        assertThat(hearingDate).isEqualTo(simpleDate);
    }

    @Test
    void shouldGetNearestHearingDateInFuture() throws ParseException {
        // Given
        CaseData caseData = caseTestData.getCaseData();

        String futureDate = LocalDateTime.now().plusDays(5).toString();
        caseData.getHearingCollection().getFirst().getValue()
            .getHearingDateCollection().getFirst().getValue().setListedDate(futureDate);

        Date hearingStartDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.ENGLISH).parse(futureDate);
        String simpleDate = new SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH).format(hearingStartDate);

        // When
        String hearingDate = NotificationsHelper.getEarliestDateForHearing(
            caseData.getHearingCollection(),
            "123345"
        );

        // Then
        assertThat(hearingDate).isEqualTo(simpleDate);
    }

    @Test
    void shouldNotFindHearingAndThrowError() {

        caseTestData.getCaseData().setHearingCollection(new ArrayList<>());
        var hearingCollection = caseTestData.getCaseData().getHearingCollection();

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            NotificationsHelper.getEarliestDateForHearing(
                hearingCollection,
                "123345"
            ));
        assertThat(exception.getMessage()).isEqualTo("Hearing does not exist in hearing collection");
    }

    @Test
    void shouldNotFindHearingDateInFutureAndThrowError() {
        List<HearingTypeItem> hearingCollection = caseTestData.getCaseData().getHearingCollection();

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            NotificationsHelper.getEarliestDateForHearing(
                hearingCollection,
                "123345"
            ));
        assertThat(exception.getMessage()).isEqualTo("Hearing does not have any future dates");
    }

    @ParameterizedTest
    @MethodSource("caseParametersForIsRepresentedClaimantWithMyHmctsCaseParameter")
    void isRepresentedClaimantWithMyHmctsCase(String caseSource, String claimantRepresentedQuestion,
                                              RepresentedTypeC representedTypeC, boolean expected) {
        CaseData caseData = new CaseData();
        caseData.setCaseSource(caseSource);
        caseData.setClaimantRepresentedQuestion(claimantRepresentedQuestion);
        caseData.setRepresentativeClaimantType(representedTypeC);
        assertEquals(expected, NotificationsHelper.isRepresentedClaimantWithMyHmctsCase(caseData));
    }

    private static Stream<Arguments> caseParametersForIsRepresentedClaimantWithMyHmctsCaseParameter() {
        Organisation organisation = Organisation.builder()
            .organisationID("dummyId")
            .build();
        RepresentedTypeC representedTypeC = new RepresentedTypeC();
        representedTypeC.setMyHmctsOrganisation(organisation);

        RepresentedTypeC representedTypeCWithoutOrganisation = new RepresentedTypeC();
        return Stream.of(
            Arguments.of(ET1, NO, null, false),
            Arguments.of(ET1, NO, representedTypeC, false),
            Arguments.of(ET1, YES, null, false),
            Arguments.of(ET1, YES, representedTypeC, false),
            Arguments.of(MY_HMCTS, NO, null, false),
            Arguments.of(MY_HMCTS, NO, representedTypeC, false),
            Arguments.of(MY_HMCTS, YES, null, false),
            Arguments.of(MY_HMCTS, YES, representedTypeC, true),
            Arguments.of(ET1, NO, null, false),
            Arguments.of(ET1, NO, representedTypeCWithoutOrganisation, false),
            Arguments.of(ET1, YES, null, false),
            Arguments.of(ET1, YES, representedTypeCWithoutOrganisation, false),
            Arguments.of(MY_HMCTS, NO, null, false),
            Arguments.of(MY_HMCTS, NO, representedTypeCWithoutOrganisation, false),
            Arguments.of(MY_HMCTS, YES, null, false),
            Arguments.of(MY_HMCTS, YES, representedTypeCWithoutOrganisation, false)
        );
    }
}
