package uk.gov.hmcts.reform.et.syaapi.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

import java.util.Map;

/**
 * Stores a {@link Map} which is used to pass case data and id within a http request.
 */
@Data
@Builder
@Jacksonized
public class CaseRequest {
    @JsonProperty("case_id")
    private String caseId;

    @JsonProperty("case_type_id")
    private String caseTypeId;

    @JsonProperty("post_code")
    private String postCode;

    @JsonProperty("case_data")
    private Map<String, Object> caseData;
}
