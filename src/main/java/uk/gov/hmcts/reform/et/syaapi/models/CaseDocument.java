package uk.gov.hmcts.reform.et.syaapi.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

import java.net.URI;
import java.util.Map;

/**
 * Defines the Document object returned from the CCD Document AM API.
 */
@Data
@Builder
@Jacksonized
public class CaseDocument {
    @JsonProperty("classification")
    String classification;
    @JsonProperty("size")
    String size;
    @JsonProperty("mimeType")
    String mimeType;
    @JsonProperty("originalDocumentName")
    String originalDocumentName;
    @JsonProperty("hashToken")
    String hashToken;
    @JsonProperty("createdOn")
    String createdOn;
    @JsonProperty("createdBy")
    String createdBy;
    @JsonProperty("lastModifiedBy")
    String lastModifiedBy;
    @JsonProperty("modifiedOn")
    String modifiedOn;
    @JsonProperty("ttl")
    String ttl;
    @JsonProperty("metadata")
    Map<String, String> metadata;
    @JsonProperty("_links")
    Map<String, Map<String, String>> links;

    public URI getUri() {
        return URI.create(links.get("self").get("href"));
    }
}
