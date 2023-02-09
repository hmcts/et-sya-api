package uk.gov.hmcts.reform.et.syaapi.models;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.et.common.model.ccd.types.UploadedDocumentType;

@Data
@Builder
public class GenericTseApplication implements TornadoDocument {
    String applicationType;
    String tellOrAskTribunal;
    String supportingEvidence;
    String copyToOtherPartyYesOrNo;
    String copyToOtherPartyText;
}
