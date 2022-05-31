package uk.gov.hmcts.reform.et.syaapi.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

import java.util.Date;
import java.util.Map;

@Data
@Builder
@Jacksonized
public class CaseDocument {
    @JsonProperty("originalDocumentName")
    String originalDocumentName;

    @JsonProperty("links")
    Map<String, Map<String, String>> links;
}
