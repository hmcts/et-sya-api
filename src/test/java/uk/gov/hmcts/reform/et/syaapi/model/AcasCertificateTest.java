package uk.gov.hmcts.reform.et.syaapi.model;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.et.syaapi.models.AcasCertificate;

import static org.assertj.core.api.Assertions.assertThat;

class AcasCertificateTest {

    public static final String CASE_NUM = "123456";
    public static final String TEST_DOC = "TEST DOC";

    @Test
    void acasCertMeetsEqualsContract() {
        EqualsVerifier.simple()
            .forClass(AcasCertificate.class)
            .verify();
    }

    @Test
    void certificateNumberGetsCorrectly() {
        AcasCertificate acasCertificate = new AcasCertificate();
        ReflectionTestUtils.setField(acasCertificate, "certificateNumber", CASE_NUM);

        assertThat(acasCertificate.getCertificateNumber())
            .isEqualTo(CASE_NUM);
    }

    @Test
    void certificateDocumentGetsCorrectly() {
        AcasCertificate acasCertificate = new AcasCertificate();
        ReflectionTestUtils.setField(acasCertificate, "certificateDocument", TEST_DOC);

        assertThat(acasCertificate.getCertificateDocument())
            .isEqualTo(TEST_DOC);
    }

    @Test
    void certificateNumberSetsCorrectly() {
        AcasCertificate acasCertificate = new AcasCertificate();
        acasCertificate.setCertificateNumber(CASE_NUM);

        assertThat((String) ReflectionTestUtils.invokeMethod(acasCertificate, "getCertificateNumber"))
            .isEqualTo(CASE_NUM);
    }

    @Test
    void certificateDocumentSetsCorrectly() {
        AcasCertificate acasCertificate = new AcasCertificate();
        acasCertificate.setCertificateDocument(TEST_DOC);

        assertThat((String) ReflectionTestUtils.invokeMethod(acasCertificate, "getCertificateDocument"))
            .isEqualTo(TEST_DOC);
    }
}
