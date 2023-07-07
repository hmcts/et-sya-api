package uk.gov.hmcts.reform.et.syaapi.helper;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTseApplicationType;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTseApplicationTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.TseAdminRecordDecisionTypeItem;
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
        private static final String ID = "testId";

        @Test
        void correctApplicationWhenExists() {
            GenericTseApplicationTypeItem expected = GenericTseApplicationTypeItem.builder().id(ID).build();
            List<GenericTseApplicationTypeItem> applications = List.of(expected);

            GenericTseApplicationTypeItem actual = TseApplicationHelper.getSelectedApplication(applications, ID);

            assertThat(actual).isEqualTo(expected);
        }

        @Test
        void nullWhenDoesNotExist() {
            String wrongId = "wrongId";
            GenericTseApplicationTypeItem expected = GenericTseApplicationTypeItem.builder().id(ID).build();
            List<GenericTseApplicationTypeItem> applications = List.of(expected);

            GenericTseApplicationTypeItem actual = TseApplicationHelper.getSelectedApplication(applications, wrongId);

            assertThat(actual).isNull();
        }
    }

    @Nested
    class SetRespondentApplicationWithResponse {
        @Test
        void applicationStatusWaitingForTheTribunal() {
            RespondToApplicationRequest request = data.getRespondToApplicationRequest();
            GenericTseApplicationType app = GenericTseApplicationType.builder().build();
            CaseData caseData = data.getCaseData();
            CaseDocumentService service = mock(CaseDocumentService.class);

            TseApplicationHelper.setRespondentApplicationWithResponse(request, app, caseData, service);

            Assertions.assertEquals("waitingForTheTribunal", app.getApplicationState());
        }
    }

    @Nested
    class FindAdminDecisions {
        @Test
        void shouldFindAdminDecision() {
            GenericTseApplicationTypeItem app = data.getCaseData().getGenericTseApplicationCollection().get(0);
            String decisionId = "777";

            TseAdminRecordDecisionTypeItem result = TseApplicationHelper.findAdminDecision(app, decisionId);

            assertThat(result.getId()).isEqualTo(decisionId);
        }

        @Test
        void shouldReturnNullIfDecisionNotFound() {
            GenericTseApplicationTypeItem app = data.getCaseData().getGenericTseApplicationCollection().get(0);
            String decisionId = "778";

            TseAdminRecordDecisionTypeItem result = TseApplicationHelper.findAdminDecision(app, decisionId);

            assertThat(result).isNull();
        }
    }


}
