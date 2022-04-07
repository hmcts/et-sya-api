package uk.gov.hmcts.reform.et.syaapi.model;

import java.util.Objects;

/**
 * Simply holds the certificate string along with the associated ACAS number.
 */
public class AcasCertificate {

    private String certificateDocument;
    private String certificateNumber;

    public AcasCertificate(String certificateData, String acasNumber) {
        this.certificateDocument = certificateData;
        this.certificateNumber = acasNumber;
    }

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
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AcasCertificate that = (AcasCertificate) o;
        return Objects.equals(certificateDocument, that.certificateDocument)
            && Objects.equals(certificateNumber, that.certificateNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(certificateDocument, certificateNumber);
    }
}
