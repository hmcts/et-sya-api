package uk.gov.hmcts.reform.et.syaapi.helper;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.DocumentTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTseApplicationType;
import uk.gov.hmcts.et.common.model.ccd.items.ListTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.TypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.DocumentType;
import uk.gov.hmcts.et.common.model.ccd.types.TseAdminRecordDecisionType;
import uk.gov.hmcts.reform.et.syaapi.model.TestData;
import uk.gov.hmcts.reform.et.syaapi.models.RespondToApplicationRequest;
import uk.gov.hmcts.reform.et.syaapi.service.CaseDocumentService;

import java.time.LocalDate;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TseApplicationHelperTest {
    @MockBean
    private CaseDocumentService caseDocumentService;

    TestData data = new TestData();

    @Nested
    class GetSelectedApplication {
        private static final String ID = "testId";

        @Test
        void correctApplicationWhenExists() {
            TypeItem<GenericTseApplicationType> expected = TypeItem.<GenericTseApplicationType>builder().id(ID).build();
            ListTypeItem<GenericTseApplicationType> applications = ListTypeItem.from(expected);

            TypeItem<GenericTseApplicationType> actual = TseApplicationHelper.getSelectedApplication(applications, ID);

            assertThat(actual).isEqualTo(expected);
        }

        @Test
        void nullWhenDoesNotExist() {
            String wrongId = "wrongId";
            TypeItem<GenericTseApplicationType> expected = TypeItem.<GenericTseApplicationType>builder().id(ID).build();
            ListTypeItem<GenericTseApplicationType> applications = ListTypeItem.from(expected);

            TypeItem<GenericTseApplicationType> actual = 
                TseApplicationHelper.getSelectedApplication(applications, wrongId);

            assertThat(actual).isNull();
        }
    }

    @Nested
    class SetRespondentApplicationWithResponse {
        @Test
        void applicationStatusWaitingForTheTribunal() {
            RespondToApplicationRequest request = data.getRespondToApplicationRequest();
            GenericTseApplicationType app = GenericTseApplicationType.builder().type("Amend Response").build();
            CaseData caseData = data.getCaseData();
            caseDocumentService = mock(CaseDocumentService.class);
            DocumentTypeItem docType = DocumentTypeItem.builder().id("1").value(new DocumentType()).build();
            when(caseDocumentService.createDocumentTypeItem(any(), any())).thenReturn(docType);

            TseApplicationHelper.setRespondentApplicationWithResponse(request, app, caseData, caseDocumentService);

            Assertions.assertEquals("waitingForTheTribunal", app.getApplicationState());
            Assertions.assertEquals("Response to Amend Response",
                caseData.getDocumentCollection().get(0).getValue().getShortDescription()
            );
        }
    }

    @Nested
    class FindAdminDecisions {
        @Test
        void shouldFindAdminDecision() {
            TypeItem<GenericTseApplicationType> app = data.getCaseData().getGenericTseApplicationCollection().get(0);
            String decisionId = "777";

            TypeItem<TseAdminRecordDecisionType> result = TseApplicationHelper.findAdminDecision(app, decisionId);

            assertThat(result.getId()).isEqualTo(decisionId);
        }

        @Test
        void shouldReturnNullIfDecisionNotFound() {
            TypeItem<GenericTseApplicationType> app = data.getCaseData().getGenericTseApplicationCollection().get(0);
            String decisionId = "778";

            TypeItem<TseAdminRecordDecisionType> result = TseApplicationHelper.findAdminDecision(app, decisionId);

            assertThat(result).isNull();
        }
    }

    @Nested
    class FindResponses {
        @Test
        void shouldFindAdminResponse() {
            TypeItem<GenericTseApplicationType> app = data.getCaseData().getGenericTseApplicationCollection().get(0);
            String responseId = "777";

            var result = TseApplicationHelper.findResponse(app, responseId);

            assertThat(result.getId()).isEqualTo(responseId);
        }

        @Test
        void shouldReturnNullIfResponseNotFound() {
            TypeItem<GenericTseApplicationType> app = data.getCaseData().getGenericTseApplicationCollection().get(0);
            String responseId = "778";

            var result = TseApplicationHelper.findResponse(app, responseId);

            assertThat(result).isNull();
        }
    }

    @Nested
    class FormatCurrentDate {
        @Test
        void shouldReturnDateTimeFormattedInUkPattern() {
            String expectedLocalDateTime = "07 Feb 2022";
            LocalDate data = LocalDate.of(2022, 2, 7);
            String actualLocalDateTime = TseApplicationHelper.formatCurrentDate(data);

            assertThat(actualLocalDateTime).isEqualTo(expectedLocalDateTime);
        }
    }
}
