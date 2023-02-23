package uk.gov.hmcts.reform.et.syaapi.helper;

import org.apache.tika.utils.StringUtils;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.RepresentedTypeRItem;
import uk.gov.hmcts.et.common.model.ccd.types.RepresentedTypeR;
import uk.gov.hmcts.reform.et.syaapi.model.TestData;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class NotificationHelperTest {

    private final TestData testData;

    NotificationHelperTest() {
        testData = new TestData();
    }


    @Test
    void shouldReturnEmptyStringUtil() {
        // Given
        var data = testData.getCaseDataWithClaimTypes().getCaseData();
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
        CaseData caseData = testData.getCaseData();
        caseData.setRepCollection(itemList);

        // When
        String email = NotificationsHelper.getEmailAddressForRespondent(
            caseData,
            caseData.getRespondentCollection().get(0).getValue()
        );

        //Then
        assertThat(email).isEqualTo(repEmail);
    }
}
