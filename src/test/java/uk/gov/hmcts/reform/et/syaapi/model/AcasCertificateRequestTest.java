package uk.gov.hmcts.reform.et.syaapi.model;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.et.syaapi.models.AcasCertificateRequest;

class AcasCertificateRequestTest {

    @Test
    void acasCertRequestMeetsEqualsContract() {
        EqualsVerifier.simple()
            .forClass(AcasCertificateRequest.class)
            .verify();
    }
}
