package uk.gov.hmcts.reform.et.syaapi.service.utils;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTseApplicationType;
import uk.gov.hmcts.et.common.model.ccd.items.TypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.citizenhub.ClaimantTse;
import uk.gov.hmcts.reform.et.syaapi.models.GenericTseApplication;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
class ClaimantTseUtilTest {

    @ParameterizedTest
    @MethodSource(
        "uk.gov.hmcts.reform.et.syaapi.model.CaseTestData#generateClaimantTseArgumentsForTestingCurrentTseApplication")
    void theGetCurrentGenericTseApplication(ClaimantTse claimantTse,
                                            List<TypeItem<GenericTseApplicationType>> items,
                                            GenericTseApplication expectedGenericTseApplication,
                                            String caseReference) {
        GenericTseApplication actualGenericTseApplication = ClaimantTseUtil.getCurrentGenericTseApplication(
            claimantTse, items, caseReference);
        assertThat(actualGenericTseApplication.getCaseNumber())
            .isEqualTo(expectedGenericTseApplication.getCaseNumber());
        assertThat(actualGenericTseApplication.getApplicant())
            .isEqualTo(expectedGenericTseApplication.getApplicant());
        assertThat(actualGenericTseApplication.getApplicationType())
            .isEqualTo(expectedGenericTseApplication.getApplicationType());
        assertThat(actualGenericTseApplication.getApplicationDate())
            .isEqualTo(expectedGenericTseApplication.getApplicationDate());
        assertThat(actualGenericTseApplication.getTellOrAskTribunal())
            .isEqualTo(expectedGenericTseApplication.getTellOrAskTribunal());
        assertThat(actualGenericTseApplication.getSupportingEvidence())
            .isEqualTo(expectedGenericTseApplication.getSupportingEvidence());
        assertThat(actualGenericTseApplication.getCopyToOtherPartyYesOrNo())
            .isEqualTo(expectedGenericTseApplication.getCopyToOtherPartyYesOrNo());
        assertThat(actualGenericTseApplication.getCopyToOtherPartyText())
            .isEqualTo(expectedGenericTseApplication.getCopyToOtherPartyText());
    }
}
