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

    private Classification classification;
    private Long size;
    private String mimeType;
    private String originalDocumentName;
    private String hashToken;
    private Date createdOn;
    private String createdBy;
    private String lastModifiedBy;
    private Date modifiedOn;
    private Date ttl;
    private Map<String, String> metadata;

}
