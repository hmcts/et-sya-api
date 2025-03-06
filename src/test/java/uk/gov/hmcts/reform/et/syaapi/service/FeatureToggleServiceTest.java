package uk.gov.hmcts.reform.et.syaapi.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ecm.common.launchdarkly.FeatureToggleApi;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FeatureToggleServiceTest {

    @Mock
    private FeatureToggleApi featureToggleApi;

    private FeatureToggleService featureToggleService;

    @BeforeEach
    void setUp() {
        featureToggleService = new FeatureToggleService(featureToggleApi);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldReturnCorrectValueWhenAnyFeatureIsEnabled(boolean toggleStat) {
        var caseFileKey = "any-feature";
        givenToggle(caseFileKey, toggleStat);

        assertThat(featureToggleService.isFeatureEnabled(caseFileKey)).isEqualTo(toggleStat);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldReturnCorrectValueWhenCaseFlagsLinkingIsEnabled(boolean toggleStat) {
        givenToggle("case-flags-linking-enabled", toggleStat);

        assertThat(featureToggleService.isCaseFlagsEnabled()).isEqualTo(toggleStat);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldReturnCorrectValueWhenWorkAllocationIsEnabled(boolean toggleStat) {
        givenToggle("work-allocation", toggleStat);

        assertThat(featureToggleService.isWorkAllocationEnabled()).isEqualTo(toggleStat);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldReturnCorrectValueWhenWelshIsEnabled(boolean toggleStat) {
        givenToggle("welsh-language", toggleStat);

        assertThat(featureToggleService.isWelshEnabled()).isEqualTo(toggleStat);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldReturnCorrectValueWhenEccIsEnabled(boolean toggleStat) {
        givenToggle("ecc", toggleStat);

        assertThat(featureToggleService.isEccEnabled()).isEqualTo(toggleStat);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldReturnCorrectValueWhenCitizenEt1GenerationIsEnabled(boolean toggleStat) {
        givenToggle("citizen-et1-generation", toggleStat);

        assertThat(featureToggleService.citizenEt1Generation()).isEqualTo(toggleStat);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldReturnCorrectValueWhenAcasVetAndAcceptIsEnabled(boolean toggleStat) {
        givenToggle("acasVetAndAccept", toggleStat);

        assertThat(featureToggleService.isAcasVetAndAcceptEnabled()).isEqualTo(toggleStat);
    }

    private void givenToggle(String feature, boolean state) {
        when(featureToggleApi.isFeatureEnabled(feature)).thenReturn(state);
    }
}
