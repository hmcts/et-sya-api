package uk.gov.hmcts.reform.et.syaapi.service.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;

import java.util.List;

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
    void putRespondent(List<RespondentSumTypeItem> respondentList) {
        caseData.setRespondentCollection(respondentList);
        assertThat(caseData.getRespondentCollection()).isNull();
    }

}
