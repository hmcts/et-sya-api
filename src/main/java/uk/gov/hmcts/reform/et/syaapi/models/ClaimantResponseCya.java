package uk.gov.hmcts.reform.et.syaapi.models;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ClaimantResponseCya implements TornadoDocument {
    String response;
    String fileName;
    String copyToOtherPartyYesOrNo;
}
