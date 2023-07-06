package uk.gov.hmcts.reform.et.syaapi.models;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

class AcasCertificateRequestTest {

    @Test
    void acasCertRequestMeetsEqualsContract() {
        EqualsVerifier.simple()
            .forClass(AcasCertificateRequest.class)
            .verify();
    }
}
