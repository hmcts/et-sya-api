package uk.gov.hmcts.reform.et.syaapi.service;

import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.ecm.common.exceptions.DocumentManagementException;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.document.am.feign.CaseDocumentClient;
import uk.gov.hmcts.reform.ccd.document.am.model.Classification;

import java.net.URI;

import static java.util.Collections.singletonList;

/**
 * CaseDocumentService provides access to the document
 * upload service API
 */
@Slf4j
@Service
public class CaseDocumentService {
    private static final String JURISDICTION = "EMPLOYMENT";
    private final AuthTokenGenerator authTokenGenerator;
    private final CaseDocumentClient caseDocumentClient;

    @Autowired
    public CaseDocumentService(AuthTokenGenerator authTokenGenerator, CaseDocumentClient caseDocumentClient) {
        this.authTokenGenerator = authTokenGenerator;
        this.caseDocumentClient = caseDocumentClient;
    }

    @Retryable({FeignException.class, RuntimeException.class, DocumentManagementException.class})
    public URI uploadDocument(String authToken, String caseTypeId, MultipartFile file) {
        log.info("Using Case Document Client");
        try {
            var response = caseDocumentClient.uploadDocuments(
                authToken,
                authTokenGenerator.generate(),
                caseTypeId,
                JURISDICTION,
                singletonList(file),
                Classification.PUBLIC
            );

            var document = response.getDocuments().stream()
                .findFirst()
                .orElseThrow(() ->
                                 new DocumentManagementException("Document management failed uploading file"
                                                                     + file.getOriginalFilename()));
            log.info("Uploaded document successful");
            return URI.create(document.links.self.href);
        } catch (Exception ex) {
            log.info("Exception: " + ex.getMessage());
            throw new DocumentManagementException(String.format(
                "Unable to upload document %s to document management",
                file.getOriginalFilename()
            ), ex);
        }
    }
}
