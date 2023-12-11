package uk.gov.hmcts.reform.et.syaapi.service.utils;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTseApplicationTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.citizenhub.ClaimantTse;
import uk.gov.hmcts.reform.et.syaapi.models.GenericTseApplication;
import uk.gov.hmcts.reform.et.syaapi.service.utils.data.TestDataProvider;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
class ClaimantTseUtilTest {

    @ParameterizedTest
    @MethodSource(
        "uk.gov.hmcts.reform.et.syaapi.model.CaseTestData#generateClaimantTseArgumentsForTestingCurrentTseApplication")
    void theGetCurrentGenericTseApplication(ClaimantTse claimantTse,
                                            List<GenericTseApplicationTypeItem> items,
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

    @ParameterizedTest
    @MethodSource("generateClaimantTseArgumentsForTestingStoredGenericTseApplication")
    void theGetCurrentStoredGenericTseApplication(GenericTseApplicationTypeItem item,
                                               GenericTseApplication expectedGenericTseApplication,
                                               String caseReference) {
        GenericTseApplication actualGenericTseApplication = ClaimantTseUtil.getCurrentStoredGenericTseApplication(
            item, caseReference);
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

    private static Stream<Arguments> generateClaimantTseArgumentsForTestingStoredGenericTseApplication() {
        String caseReference = "1234/456";
        List<String> completeArgumentsList = List.of(
            "Mr Test Applicant",
            "1",
            "test Contact Application Type",
            "13 Jan 2023",
            "Yes",
            "3",
            "test-Document-Filename.pdf",
            caseReference
        );

        GenericTseApplication completeExpectedTseApp = TestDataProvider.generateExpectedTseApp(completeArgumentsList);

        GenericTseApplicationTypeItem completeTseItem =
            TestDataProvider.generateStoredGenericTseAppTypeItem(completeArgumentsList);

        return Stream.of(Arguments.of(
            completeTseItem,
            completeExpectedTseApp,
            caseReference
        ));
    }
}
