package uk.gov.hmcts.reform.et.syaapi.models;

import java.util.Date;
import java.util.Map;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.ccd.client.model.Classification;

@Data
//@Builder
//@Jacksonized
public class DocumentDetailsResponse {

    Classification classification;
    Long size;
    String mimeType;
    String originalDocumentName;
    String hashToken;
    Date createdOn;
    String createdBy;
    String lastModifiedBy;
    Date modifiedOn;
    Date ttl;
    Map<String, String> metadata;
}
