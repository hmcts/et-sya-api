package uk.gov.hmcts.reform.et.syaapi.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

import java.util.Map;

/**
 * Defines the Document object returned from the CCD Document AM API.
 */
@Data
@Builder
@Jacksonized
public class CaseDocument {
    @JsonProperty("originalDocumentName")
    String originalDocumentName;

    @JsonProperty("_links")
    Map<String, Map<String, String>> links;
}
