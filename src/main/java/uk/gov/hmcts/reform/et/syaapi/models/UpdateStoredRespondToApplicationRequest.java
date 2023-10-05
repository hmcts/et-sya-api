package uk.gov.hmcts.reform.et.syaapi.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

@Data
@Builder
@Jacksonized
public class UpdateStoredRespondToApplicationRequest {

    @JsonProperty("case_id")
    private String caseId;
    @JsonProperty("case_type_id")
    private String caseTypeId;
    @JsonProperty("application_id")
    private String applicationId;
    @JsonProperty("response_id")
    private String respondId;
    @JsonProperty("isRespondingToRequestOrOrder")
    private boolean isRespondingToRequestOrOrder;
}
