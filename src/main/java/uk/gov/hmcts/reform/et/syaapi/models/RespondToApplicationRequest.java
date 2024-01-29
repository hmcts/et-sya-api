package uk.gov.hmcts.reform.et.syaapi.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;
import uk.gov.hmcts.et.common.model.ccd.types.TseRespond;
import uk.gov.hmcts.et.common.model.ccd.types.UploadedDocumentType;

@Data
@Builder
@Jacksonized
public class RespondToApplicationRequest {

    @JsonProperty("case_id")
    private String caseId;
    @JsonProperty("case_type_id")
    private String caseTypeId;
    @JsonProperty("applicationId")
    private String applicationId;
    @JsonProperty("supportingMaterialFile")
    private UploadedDocumentType supportingMaterialFile;
    @JsonProperty("response")
    private TseRespond response;
    @JsonProperty("isRespondingToRequestOrOrder")
    private boolean isRespondingToRequestOrOrder;
}
