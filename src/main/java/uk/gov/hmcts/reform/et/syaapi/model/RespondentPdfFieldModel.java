package uk.gov.hmcts.reform.et.syaapi.model;

public record RespondentPdfFieldModel(String respondentNameFieldName, String respondentAddressFieldName,
                                      String respondentPostcodeFieldName,
                                      AcasCertificatePdfFieldModel respondentAcasCertificatePdfFieldModel) {
}
