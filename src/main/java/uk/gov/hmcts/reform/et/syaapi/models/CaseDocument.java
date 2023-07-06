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

    /**
     * Retrives the link for the document that has been uploaded to dm-store.
     * @return a link to the document uploaded wrapped within a URI object
     */
    public URI getUri() {
        return URI.create(links.get("self").get("href"));
    }

    /**
     * verifies that the link for the document stored within dm-store exists.
     * @return a boolean that is true if the document link exists
     */
    public boolean verifyUri() {
        return !(links == null
            || links.get("self") == null
            || links.get("self").get("href") == null);
    }
}
