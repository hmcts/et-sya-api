package uk.gov.hmcts.reform.et.syaapi.service.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.reform.et.syaapi.model.TestData;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class PdfMapperRespondentUtilTest {

    private CaseData caseData;

    @BeforeEach
    void beforeEach() {
        caseData = new CaseData();
        caseData.setEthosCaseReference("1234567890");
    }

    @ParameterizedTest
    @NullSource
    @MethodSource("retrieveRespondentSumTypes")
    void putRespondent(CaseData respondentCaseData) {

        ConcurrentMap<String, Optional<String>> printFields = new ConcurrentHashMap<>();
        PdfMapperRespondentUtil.putRespondents(respondentCaseData, printFields);
        assertThat(caseData.getRespondentCollection()).isNull();
    }

    private static Stream<Arguments> retrieveRespondentSumTypes() {
        return TestData.generateRespondentSumTypeItems();
    }
}
