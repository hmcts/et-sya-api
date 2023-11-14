package uk.gov.hmcts.reform.et.syaapi.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ecm.common.launchdarkly.FeatureToggleApi;

@Slf4j
@Service
public class FeatureToggleService {

    private final FeatureToggleApi featureToggleApi;

    @Autowired
    public FeatureToggleService(FeatureToggleApi featureToggleApi) {
        this.featureToggleApi = featureToggleApi;
    }


    public boolean isFeatureEnabled(String feature) {
        return this.featureToggleApi.isFeatureEnabled(feature);
    }

    public boolean isCaseFlagsEnabled() {
        return this.featureToggleApi.isFeatureEnabled("case-flags-linking-enabled");
    }

    public boolean isBundlesEnabled() {
        return this.featureToggleApi.isFeatureEnabled("bundles");
    }

    public boolean isWorkAllocationEnabled() {
        return this.featureToggleApi.isFeatureEnabled("work-allocation");
    }
}
