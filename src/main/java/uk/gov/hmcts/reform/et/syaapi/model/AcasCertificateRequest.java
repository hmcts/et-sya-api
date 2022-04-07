package uk.gov.hmcts.reform.et.syaapi.model;

public class AcasCertificateRequest {

    private String[] certificateNumbers;

    public AcasCertificateRequest(String[] certificateNumbers) {
        this.certificateNumbers = certificateNumbers;
    }

    public String[] getCertificateNumbers() {
        return certificateNumbers;
    }

    public void setCertificateNumbers(String[] certificateNumbers) {
        this.certificateNumbers = certificateNumbers;
    }
}
