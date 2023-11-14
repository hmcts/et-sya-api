package uk.gov.hmcts.reform.et.syaapi.helper;

import org.apache.tika.utils.StringUtils;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.RepresentedTypeRItem;
import uk.gov.hmcts.et.common.model.ccd.types.RepresentedTypeR;
import uk.gov.hmcts.reform.et.syaapi.model.CaseTestData;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

class NotificationHelperTest {

    public static final String NOT_SET = "Not set";
    private final CaseTestData caseTestData;

    NotificationHelperTest() {
        caseTestData = new CaseTestData();
    }

    @Test
    void shouldReturnEmptyStringUtil() {
        // Given
        var data = caseTestData.getCaseDataWithClaimTypes().getCaseData();
        CaseData caseData = EmployeeObjectMapper.mapRequestCaseDataToCaseData(data);
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
        String email = NotificationsHelper.getEmailAddressForRespondent(
            caseData,
            caseData.getRespondentCollection().get(0).getValue()
        );

        //Then
        assertThat(email).isEqualTo(repEmail);
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
        caseData.getRespondentCollection().get(0).getValue().setRespondentEmail(null);
        caseData.setRepCollection(itemList);

        // When
        String email = NotificationsHelper.getEmailAddressForRespondent(
            caseData,
            caseData.getRespondentCollection().get(0).getValue()
        );

        // Then
        assertThat(email).isEmpty();
    }

    @Test
    void shouldNotReturnRespondentEmail() {
        // Given
        CaseData caseData = caseTestData.getCaseData();
        caseData.getRespondentCollection().get(0).getValue().setRespondentEmail(null);

        // When
        String email = NotificationsHelper.getEmailAddressForRespondent(
            caseData,
            caseData.getRespondentCollection().get(0).getValue()
        );

        // Then
        assertThat(email).isEmpty();
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
        caseData.getHearingCollection().get(0).getValue().getHearingDateCollection().get(0).getValue().setListedDate(
            futureDate);

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
        caseData.getHearingCollection().get(0).getValue().getHearingDateCollection().get(0).getValue().setListedDate(
            futureDate);

        Date hearingStartDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.ENGLISH).parse(futureDate);
        String simpleDate = new SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH).format(hearingStartDate);

        // When
        String hearingDate = NotificationsHelper.getEarliestDateForHearing(
            caseData.getHearingCollection(),
            "123345",
            NOT_SET
        );

        // Then
        assertThat(hearingDate).isEqualTo(simpleDate);
    }
}
