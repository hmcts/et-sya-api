package uk.gov.hmcts.reform.et.syaapi.models;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uk.gov.hmcts.reform.ccd.client.model.Classification;

import java.util.Date;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
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
