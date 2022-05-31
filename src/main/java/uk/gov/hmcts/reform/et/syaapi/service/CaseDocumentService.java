package uk.gov.hmcts.reform.et.syaapi.service;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.ecm.common.exceptions.DocumentManagementException;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.Classification;
import uk.gov.hmcts.reform.et.syaapi.models.CaseDocument;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Objects;

/**
 * CaseDocumentService provides access to the document upload service API.
 * This relies upon the following configurations to be set at an environment level:
 * <ul>
 *     <li>case_document_am.url</li>
 * </ul>
 */
@Service
public class CaseDocumentService {
    private static final String JURISDICTION = "EMPLOYMENT";

    private static final String SERVICE_AUTHORIZATION = "ServiceAuthorization";

    private final RestTemplate restTemplate;

    private final AuthTokenGenerator authTokenGenerator;

    private final String caseDocApiUrl;

    public CaseDocumentService(RestTemplate restTemplate,
                               AuthTokenGenerator authTokenGenerator,
                               @Value("${case_document_am.url}/cases/documents}") String caseDocApiUrl) {
        this.restTemplate = restTemplate;
        this.authTokenGenerator = authTokenGenerator;
        this.caseDocApiUrl = caseDocApiUrl;
    }

    /**
     * When given a file to upload, this call with upload the file to the CCD document API.
     * return a URI pointing to the uploaded file if successful.
     * @param authToken the caller's bearer token used to verify the caller
     * @param caseTypeId defines the area the file belongs to e.g. ET_EnglandWales
     * @param file the file to be uploaded
     * @return the URL of the document we have just uploaded
     * @throws DocumentManagementException  if a problem occurs whilst uploading the document via API
     */
    public URI uploadDocument(String authToken, String caseTypeId, MultipartFile file) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.AUTHORIZATION, authToken);
        headers.add(SERVICE_AUTHORIZATION, authTokenGenerator.generate());
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        try {
            MultiValueMap<String, Object> body = generateUploadRequest(
                Classification.PUBLIC, caseTypeId, JURISDICTION, file);
            HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(body, headers);
            ResponseEntity<DocumentUploadResponse> response = restTemplate.exchange(
                caseDocApiUrl,
                HttpMethod.POST,
                request,
                DocumentUploadResponse.class
            );
            CaseDocument caseDocument = validateDocument(
                Objects.requireNonNull(response.getBody()), file.getOriginalFilename());
            return URI.create(caseDocument.getLinks().get("self").get("href"));
        } catch (RestClientException e) {
            throw new DocumentManagementException("Failed to connect with case document upload API", e);
        } catch (IOException e) {
            throw new DocumentManagementException("Failed serialize multipartFile", e);
        }
    }

    private CaseDocument validateDocument(DocumentUploadResponse response, String originalFilename) {
        return response.getDocuments().stream()
            .findFirst()
            .orElseThrow(() ->
                             new DocumentManagementException("Document management failed uploading file: "
                                                                 + originalFilename));
    }

    private MultiValueMap<String, Object> generateUploadRequest(Classification classification,
                                                                String caseTypeId,
                                                                String jurisdictionId,
                                                                MultipartFile file) throws IOException {
        ByteArrayResource fileAsResource = new ByteArrayResource(file.getBytes()) {
            @Override
            public String getFilename() {
                return file.getOriginalFilename();
            }
        };

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", fileAsResource);
        body.add("fileName", file.getOriginalFilename());
        body.add("classification", classification);
        body.add("caseTypeId", caseTypeId);
        body.add("jurisdictionId", jurisdictionId);

        return body;
    }

    @Data
    private static class DocumentUploadResponse {
        private List<CaseDocument> documents;
    }
}
