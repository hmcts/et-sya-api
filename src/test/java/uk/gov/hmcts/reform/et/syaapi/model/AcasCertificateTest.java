package uk.gov.hmcts.reform.et.syaapi.model;

import org.junit.jupiter.api.Test;
import nl.jqno.equalsverifier.EqualsVerifier;

class AcasCertificateTest {

    @Test
    void acasCertMeetsEqualsContract() {
        EqualsVerifier.simple()
            .forClass(AcasCertificate.class)
            .verify();
    }
}
