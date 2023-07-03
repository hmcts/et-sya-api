package uk.gov.hmcts.reform.et.syaapi.service.pdf.util;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import uk.gov.hmcts.et.common.model.ccd.CaseData;

class PdfMapperClaimDetailsUtilTest {

    @ParameterizedTest
    @MethodSource("uk.gov.hmcts.reform.et.syaapi.service.util.data.PdfMapperTestDataProvider#generateClaimantRequests")
    void putClaimDetails(CaseData caseData) {

    }

}
