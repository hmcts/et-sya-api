package uk.gov.hmcts.reform.et.syaapi.service.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.reform.et.syaapi.model.TestData;

import java.util.stream.Stream;

public class PdfMapperHearingPreferencesUtilTest {

    private CaseData caseData;

    @BeforeEach
    void beforeEach() {
        caseData = new CaseData();
        caseData.setEthosCaseReference("1234567890");
    }

    @ParameterizedTest
    @NullSource
    @MethodSource("retrieveCaseDataSamplesWithHearingPreferences")
    void putHearingPreferences() {

    }

    private static Stream<Arguments> retrieveCaseDataSamplesWithHearingPreferences() {
        return TestData.generateCaseDataSamplesWithRespondentSumTypeItems();
    }
}
