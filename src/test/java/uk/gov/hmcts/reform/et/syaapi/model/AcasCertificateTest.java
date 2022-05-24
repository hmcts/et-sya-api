package uk.gov.hmcts.reform.et.syaapi.model;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.et.syaapi.models.AcasCertificate;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.et.syaapi.utils.TestConstants.TEST_STRING;

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
        assertThat(NUM_STRING).isEqualTo(acasCertificate.getCertificateNumber());

    }

    @Test
    void testAcasCertificateDocument() {
        AcasCertificate acasCertificate = new AcasCertificate();
        acasCertificate.setCertificateDocument(TEST_STRING);
        acasCertificate.setCertificateNumber(NUM_STRING);
        assertThat(TEST_STRING).isEqualTo(acasCertificate.getCertificateDocument());
    }
}
