package uk.gov.hmcts.reform.et.syaapi.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

@Data
@Builder
@Jacksonized
public class CaseDocumentAcasResponse {
    @JsonProperty("documentId")
    String documentId;
    @JsonProperty("modifiedOn")
    String modifiedOn;

}
