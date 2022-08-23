package uk.gov.hmcts.reform.et.syaapi.models;

import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;
import org.springframework.core.io.ByteArrayResource;

public class DocumentUploadRequest {
    public String jurisdictionId;
    public String classification;
    public String caseTypeId;
    public ByteArrayResource files;


    public DocumentUploadRequest(String caseTypeId, String classification, String jurisdictionId,
                                 ByteArrayResource file) {
        this.jurisdictionId = jurisdictionId;
        this.caseTypeId = caseTypeId;
        this.classification = classification;
        this.files = file;
    }
}
