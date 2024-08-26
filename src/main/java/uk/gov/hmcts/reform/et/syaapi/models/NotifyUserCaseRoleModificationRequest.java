package uk.gov.hmcts.reform.et.syaapi.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

@Data
@Builder
@Jacksonized
public class NotifyUserCaseRoleModificationRequest {
    @JsonProperty("caseSubmissionReference")
    private String caseSubmissionReference;
    @JsonProperty("role")
    private String role;
    @JsonProperty("modificationType")
    private String modificationType;
    @JsonProperty("caseType")
    private String caseType;
}
