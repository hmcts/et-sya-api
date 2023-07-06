package uk.gov.hmcts.reform.et.syaapi.models;

public record RespondentPdfFieldModel(String respondentNameFieldName, String respondentAddressFieldName,
                                      String respondentPostcodeFieldName,
                                      AcasCertificatePdfFieldModel respondentAcasCertificatePdfFieldModel) {
}
