package uk.gov.hmcts.reform.et.syaapi.models;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ClaimantResponseCya implements TornadoDocument {
    String applicant;
    String caseNumber;
    String applicationType;
    String applicationDate;
    String response;
    String fileName;
    String copyToOtherPartyYesOrNo;
}
