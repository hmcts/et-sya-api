package uk.gov.hmcts.reform.et.syaapi.models;

import lombok.Builder;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

/**
 *
 */
@Data
@Builder
public class CaseDocumentRequest {
    String caseTypeId;
    MultipartFile multipartFile;
}
