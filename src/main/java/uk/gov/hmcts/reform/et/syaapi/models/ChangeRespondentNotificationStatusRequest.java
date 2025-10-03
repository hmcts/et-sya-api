package uk.gov.hmcts.reform.et.syaapi.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

@Data
@Builder
@Jacksonized
public class ChangeRespondentNotificationStatusRequest {
    @JsonProperty("case_id")
    private String caseId;
    @JsonProperty("case_type_id")
    private String caseTypeId;
    @JsonProperty("application_id")
    private String applicationId;
    @JsonProperty("user_idam_id")
    private String userIdamId;
    @JsonProperty("new_status")
    private String newStatus;
}
