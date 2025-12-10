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

    public boolean isWelshEnabled() {
        return this.featureToggleApi.isFeatureEnabled("welsh-language");
    }

    public boolean isEccEnabled() {
        return this.featureToggleApi.isFeatureEnabled("ecc");
    }

    public boolean isMultiplesEnabled() {
        return this.featureToggleApi.isFeatureEnabled("multiples");
    }

    public boolean citizenEt1Generation() {
        return this.featureToggleApi.isFeatureEnabled("citizen-et1-generation");
    }

    /**
     * This method is used to check if the feature toggle for the ACAS vet and accept is enabled.
     * This should only be true for lower environments
     *
     * @return true if the feature toggle is enabled, false otherwise
     */
    public boolean isAcasVetAndAcceptEnabled() {
        return this.featureToggleApi.isFeatureEnabled("acasVetAndAccept");
    }

    /**
     * This method is used to check if the et3-self-assignment feature is enabled.
     * When enabled, supports professional user detection and already-assigned status.
     *
     * @return true if the feature toggle is enabled, false otherwise
     */
    public boolean isSelfAssignmentEnabled() {
        return this.featureToggleApi.isFeatureEnabled("et3-self-assignment");
    }

}
