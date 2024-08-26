package uk.gov.hmcts.reform.et.syaapi.service.utils;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.et.syaapi.model.TestData;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

class DocumentUtilTest {

    private static final String DOCUMENT_COLLECTION = "documentCollection";

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
        DocumentUtil.filterMultipleCasesDocumentsForClaimant(caseDetailsList);
        assertAll(
            () -> assertThat((List<LinkedHashMap<String, Object>>)caseDetailsFull.getData()
                .get(DOCUMENT_COLLECTION)).isNotNull().hasSize(4),
            () -> assertThat(emptyCaseDetails.getData()).isNull(),
            () -> assertThat((List<LinkedHashMap<String, Object>>)caseDetailsWithoutId.getData()
                .get(DOCUMENT_COLLECTION)).isNotNull().hasSize(4),
            () -> assertThat((List<LinkedHashMap<String, Object>>)caseDetailsWithoutId.getData()
                .get(DOCUMENT_COLLECTION)).isNotNull().hasSize(4)
        );
    }

    @Test
    @SuppressWarnings("unchecked")
    void filterCaseDocumentsForClaimant() {
        CaseDetails caseDetails = new TestData().getCaseDetailsWithData();
        DocumentUtil.filterCaseDocumentsForClaimant(caseDetails, "123");
        List<LinkedHashMap<String, Object>> documentCollection =
            (List<LinkedHashMap<String, Object>>) caseDetails.getData().get(DOCUMENT_COLLECTION);
        assertThat(documentCollection).isNotNull().hasSize(4);
    }

}
