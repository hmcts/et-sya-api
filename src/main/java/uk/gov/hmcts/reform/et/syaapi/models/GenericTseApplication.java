package uk.gov.hmcts.reform.et.syaapi.models;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GenericTseApplication implements TornadoDocument {
    String applicationType;
    String tellOrAskTribunal;
    String supportingEvidence;
    String copyToOtherPartyYesOrNo;
    String copyToOtherPartyText;
}
