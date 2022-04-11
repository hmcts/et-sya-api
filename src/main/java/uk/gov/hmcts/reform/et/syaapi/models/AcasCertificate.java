package uk.gov.hmcts.reform.et.syaapi.models;

import java.util.Objects;

/**
 * Simply holds the certificate blob data as a string (in Base64 encoded format) along with the associated ACAS number.
 */
public class AcasCertificate {

    private String certificateDocument;
    private String certificateNumber;

    public String getCertificateNumber() {
        return certificateNumber;
    }

    public void setCertificateNumber(String certificateNumber) {
        this.certificateNumber = certificateNumber;
    }

    public String getCertificateDocument() {
        return certificateDocument;
    }

    public void setCertificateDocument(String certificateDocument) {
        this.certificateDocument = certificateDocument;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        AcasCertificate that = (AcasCertificate) object;
        return Objects.equals(certificateDocument, that.certificateDocument)
            && Objects.equals(certificateNumber, that.certificateNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(certificateDocument, certificateNumber);
    }
}
