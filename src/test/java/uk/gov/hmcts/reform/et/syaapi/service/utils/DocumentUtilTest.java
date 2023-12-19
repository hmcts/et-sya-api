package uk.gov.hmcts.reform.et.syaapi.service.utils;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.et.syaapi.model.TestData;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
class DocumentUtilTest {

    @Test
    @SuppressWarnings("unchecked")
    void theFilterClaimantDocuments() {
        CaseDetails caseDetailsFull = new TestData().getCaseDetailsWithData();
        CaseDetails emptyCaseDetails = CaseDetails.builder().build();
        CaseDetails caseDetailsWithoutId = new TestData().getCaseDetailsWithData();
        CaseDetails caseDetailsWithoutDataWithId = new TestData().getCaseDetailsWithData();
        caseDetailsWithoutDataWithId.setId(123_456L);
        caseDetailsWithoutId.setId(null);
        List<CaseDetails> caseDetailsList = Arrays.asList(caseDetailsFull,
                                                          emptyCaseDetails,
                                                          caseDetailsWithoutId,
                                                          caseDetailsWithoutDataWithId);

        DocumentUtil.filterClaimantDocuments(caseDetailsList);
        assertThat((List<LinkedHashMap<String, Object>>)caseDetailsFull.getData().get("documentCollection"))
            .isNotNull().hasSize(4);
        assertThat(emptyCaseDetails.getData()).isNull();
        assertThat((List<LinkedHashMap<String, Object>>)caseDetailsWithoutId.getData().get("documentCollection"))
            .isNotNull().hasSize(4);
        assertThat((List<LinkedHashMap<String, Object>>)caseDetailsWithoutId.getData().get("documentCollection"))
            .isNotNull().hasSize(4);

    }
}
