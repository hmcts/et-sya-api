package uk.gov.hmcts.reform.et.syaapi.helper;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTseApplicationType;
import uk.gov.hmcts.et.common.model.ccd.types.TseRespondType;
import uk.gov.hmcts.reform.et.syaapi.models.RespondToApplicationRequest;
import uk.gov.hmcts.reform.et.syaapi.service.CaseDocumentService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

class TseApplicationHelperTest {

    @Test
    void givenRespApp_whenSettingClaimantResponse_thenStatusIsWaitingForTheTribunal() {
        RespondToApplicationRequest request = RespondToApplicationRequest.builder()
            .response(TseRespondType.builder().build())
            .build();

        GenericTseApplicationType app = GenericTseApplicationType.builder().build();
        CaseData caseData = new CaseData();
        CaseDocumentService service = mock(CaseDocumentService.class);

        TseApplicationHelper.setRespondentApplicationWithResponse(request, app, caseData, service);

        assertEquals("waitingForTheTribunal", app.getApplicationState());
    }
}
