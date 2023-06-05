package uk.gov.hmcts.reform.et.syaapi.helper;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTseApplicationType;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTseApplicationTypeItem;
import uk.gov.hmcts.reform.et.syaapi.model.TestData;
import uk.gov.hmcts.reform.et.syaapi.models.RespondToApplicationRequest;
import uk.gov.hmcts.reform.et.syaapi.service.CaseDocumentService;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.mock;

class TseApplicationHelperTest {

    TestData data = new TestData();

    @Nested
    class GetSelectedApplication {
        @Test
        void getSelectedApplicationWhenExists() {
            String id = "testId";
            GenericTseApplicationTypeItem expected = GenericTseApplicationTypeItem.builder().id(id).build();
            List<GenericTseApplicationTypeItem> applications = List.of(expected);
            GenericTseApplicationTypeItem actual = TseApplicationHelper.getSelectedApplication(applications, id);
            assertThat(actual).isEqualTo(expected);
        }

        @Test
        void getNullWhenSelectedApplicationDoesNotExist() {
            String id = "testId";
            String wrongId = "wrongId";
            GenericTseApplicationTypeItem expected = GenericTseApplicationTypeItem.builder().id(id).build();
            List<GenericTseApplicationTypeItem> applications = List.of(expected);
            GenericTseApplicationTypeItem actual = TseApplicationHelper.getSelectedApplication(applications, wrongId);
            assertThat(actual).isNull();
        }
    }

    @Test
    @DisplayName("given resp app, "
        + "when setting claimant response "
        + "then status is waitingForTheTribunal")
    void respAppStatusAfterClaimantResponse() {
        RespondToApplicationRequest request = data.getRespondToApplicationRequest();
        GenericTseApplicationType app = GenericTseApplicationType.builder().build();
        CaseData caseData = data.getCaseData();
        CaseDocumentService service = mock(CaseDocumentService.class);

        TseApplicationHelper.setRespondentApplicationWithResponse(request, app, caseData, service);

        Assertions.assertEquals("waitingForTheTribunal", app.getApplicationState());
    }
}
