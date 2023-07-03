package uk.gov.hmcts.reform.et.syaapi.model;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.et.syaapi.service.util.TestConstants.TEST_STRING;

class AcasCertificateTest {

    private static final String NUM_STRING = "123456";

    @Test
    void acasCertMeetsEqualsContract() {
        EqualsVerifier.simple()
            .forClass(AcasCertificate.class)
            .verify();
    }

    @Test
    void testAcasCertificateNumber() {
        AcasCertificate acasCertificate = new AcasCertificate();
        acasCertificate.setCertificateNumber(NUM_STRING);
        assertThat(acasCertificate.getCertificateNumber()).isEqualTo(NUM_STRING);

    }

    @Test
    void testAcasCertificateDocument() {
        AcasCertificate acasCertificate = new AcasCertificate();
        acasCertificate.setCertificateDocument(TEST_STRING);
        acasCertificate.setCertificateNumber(NUM_STRING);
        assertThat(acasCertificate.getCertificateDocument()).isEqualTo(TEST_STRING);
    }
}
