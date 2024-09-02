package uk.gov.hmcts.reform.et.syaapi.service.utils;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.et.syaapi.model.TestData;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static uk.gov.hmcts.reform.et.syaapi.constants.CaseRoleManagementConstants.CASE_USER_ROLE_CREATOR;
import static uk.gov.hmcts.reform.et.syaapi.constants.CaseRoleManagementConstants.CASE_USER_ROLE_DEFENDANT;

class DocumentUtilTest {

    private static final String DOCUMENT_COLLECTION = "documentCollection";

    @ParameterizedTest
    @ValueSource(strings = {CASE_USER_ROLE_CREATOR, CASE_USER_ROLE_DEFENDANT})
    @SuppressWarnings("unchecked")
    void theFilterClaimantDocuments(String caseRole) {
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
        DocumentUtil.filterCasesDocumentsByCaseUserRole(caseDetailsList, caseRole);
        assertAll(
            () -> assertThat((List<LinkedHashMap<String, Object>>)caseDetailsFull.getData()
                .get(DOCUMENT_COLLECTION)).isNotNull().hasSize(2),
            () -> assertThat(emptyCaseDetails.getData()).isNull(),
            () -> assertThat((List<LinkedHashMap<String, Object>>)caseDetailsWithoutId.getData()
                .get(DOCUMENT_COLLECTION)).isNotNull().hasSize(2),
            () -> assertThat((List<LinkedHashMap<String, Object>>)caseDetailsWithoutId.getData()
                .get(DOCUMENT_COLLECTION)).isNotNull().hasSize(2)
        );
    }
}
