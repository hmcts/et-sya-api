package uk.gov.hmcts.reform.et.syaapi.model;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.et.syaapi.models.AcasCertificate;

class AcasCertificateTest {

    @Test
    void acasCertMeetsEqualsContract() {
        EqualsVerifier.simple()
            .forClass(AcasCertificate.class)
            .verify();
    }
}
