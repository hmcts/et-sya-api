package uk.gov.hmcts.reform.et.syaapi.models;

import lombok.Data;
import uk.gov.hmcts.et.common.model.ccd.types.UploadedDocumentType;

@Data
public class GenericTseApplication implements TornadoDocument {
    String applicationType;

    String tellOrAskTribunal;

    UploadedDocumentType supportingEvidence;

    String copyToOtherPartyYesOrNo;

    String copyToOtherPartyText;
}
