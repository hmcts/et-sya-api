package uk.gov.hmcts.reform.et.syaapi.model;

public class AcasCertificatePdfFieldModel {
    String acasCertificateCheckYesFieldName;
    String acasCertificateCheckNoFieldName;
    String acasCertificateNumberFieldName;
    String noAcasReasonUnfairDismissalFieldName;
    String noAcasReasonAnotherPersonFieldName;
    String noAcasReasonNoPowerToConciliateFieldName;
    String noAcasReasonEmployerContactedFieldName;

    public AcasCertificatePdfFieldModel(String acasCertificateCheckYesFieldName,
                                        String acasCertificateCheckNoFieldName,
                                        String acasCertificateNumberFieldName,
                                        String noAcasReasonUnfairDismissalFieldName,
                                        String noAcasReasonAnotherPersonFieldName,
                                        String noAcasReasonNoPowerToConciliateFieldName,
                                        String noAcasReasonEmployerContactedFieldName) {
        this.acasCertificateCheckYesFieldName = acasCertificateCheckYesFieldName;
        this.acasCertificateCheckNoFieldName = acasCertificateCheckNoFieldName;
        this.acasCertificateNumberFieldName = acasCertificateNumberFieldName;
        this.noAcasReasonUnfairDismissalFieldName = noAcasReasonUnfairDismissalFieldName;
        this.noAcasReasonAnotherPersonFieldName = noAcasReasonAnotherPersonFieldName;
        this.noAcasReasonNoPowerToConciliateFieldName = noAcasReasonNoPowerToConciliateFieldName;
        this.noAcasReasonEmployerContactedFieldName = noAcasReasonEmployerContactedFieldName;
    }

    public String getAcasCertificateCheckYesFieldName() {
        return acasCertificateCheckYesFieldName;
    }

    public String getAcasCertificateCheckNoFieldName() {
        return acasCertificateCheckNoFieldName;
    }

    public String getAcasCertificateNumberFieldName() {
        return acasCertificateNumberFieldName;
    }

    public String getNoAcasReasonUnfairDismissalFieldName() {
        return noAcasReasonUnfairDismissalFieldName;
    }

    public String getNoAcasReasonAnotherPersonFieldName() {
        return noAcasReasonAnotherPersonFieldName;
    }

    public String getNoAcasReasonNoPowerToConciliateFieldName() {
        return noAcasReasonNoPowerToConciliateFieldName;
    }

    public String getNoAcasReasonEmployerContactedFieldName() {
        return noAcasReasonEmployerContactedFieldName;
    }

}
