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
    void shouldReturnCorrectValueWhenAnyFeatureIsEnabled(Boolean toggleStat) {
        var caseFileKey = "any-feature";
        givenToggle(caseFileKey, toggleStat);

        assertThat(featureToggleService.isFeatureEnabled(caseFileKey)).isEqualTo(toggleStat);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldReturnCorrectValueWhenCaseFlagsLinkingIsEnabled(Boolean toggleStat) {
        givenToggle("case-flags-linking-enabled", toggleStat);

        assertThat(featureToggleService.isCaseFlagsEnabled()).isEqualTo(toggleStat);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldReturnCorrectValueWhenWelshIsEnabled(Boolean toggleStat) {
        givenToggle("welsh-language", toggleStat);

        assertThat(featureToggleService.isWelshEnabled()).isEqualTo(toggleStat);
    }

    private void givenToggle(String feature, boolean state) {
        when(featureToggleApi.isFeatureEnabled(feature)).thenReturn(state);
    }
}
