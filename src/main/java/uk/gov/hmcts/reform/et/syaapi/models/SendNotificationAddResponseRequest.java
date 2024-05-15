package uk.gov.hmcts.reform.et.syaapi.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;
import uk.gov.hmcts.et.common.model.ccd.types.PseResponseType;
import uk.gov.hmcts.et.common.model.ccd.types.UploadedDocumentType;

@Data
@Builder
@Jacksonized
public class SendNotificationAddResponseRequest {
    @JsonProperty("case_id")
    private String caseId;
    @JsonProperty("case_type_id")
    private String caseTypeId;
    @JsonProperty("send_notification_id")
    private String sendNotificationId;
    @JsonProperty("supportingMaterialFile")
    private UploadedDocumentType supportingMaterialFile;
    @JsonProperty("pseResponseType")
    private PseResponseType pseResponseType;
}
