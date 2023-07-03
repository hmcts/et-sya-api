package uk.gov.hmcts.reform.et.syaapi.model;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uk.gov.hmcts.reform.ccd.client.model.Classification;
import uk.gov.hmcts.reform.et.syaapi.service.CaseDocumentService;

import java.util.Date;
import java.util.Map;

/**
 * Defines the response from successfully uploading a document with the {@link CaseDocumentService}.
 */
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
