package uk.gov.hmcts.reform.et.syaapi.service;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
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
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.et.syaapi.models.CaseDocument;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static uk.gov.hmcts.reform.ccd.client.model.Classification.PUBLIC;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.JURISDICTION_ID;

/**
 * CaseDocumentService provides access to the document upload service API, used to upload documents that are
 * associated to a specific case record held in CCD.
 * <p/>
 * This relies upon the following configurations to be set at an environment level:
 * <ul>
 *     <li>CASE_DOCUMENT_AM_URL</li>
 *     <li>CASE_DOCUMENT_AM_MAX_RETRIES</li>
 * </ul>
 */
@Slf4j
@Service
public class CaseDocumentService {
    private static final String SERVICE_AUTHORIZATION = "ServiceAuthorization";
    private static final String FILE_NAME_REGEX_PATTERN = "^[\\w\\- ]{1,256}+\\.[A-Za-z]{3,4}$";
    private static final Pattern FILE_NAME_PATTERN = Pattern.compile(FILE_NAME_REGEX_PATTERN);
    private static final String UPLOAD_FILE_EXCEPTION_MESSAGE = "Document management failed uploading file: ";
    private static final String VALIDATE_FILE_EXCEPTION_MESSAGE = "File does not pass validation";
    private final Integer maxApiRetries;
    private final RestTemplate restTemplate;
    private final AuthTokenGenerator authTokenGenerator;
    private final String caseDocApiUrl;

    /**
     * Default constructor with injected parameters.
     *
     * @param restTemplate       the {@link RestTemplate} to be used to connect with the Case Document API
     * @param authTokenGenerator the {@link AuthTokenGenerator} used to generate tokens for communicating with the
     *                           Case Document API
     * @param caseDocApiUrl      the URL to call the Case Document API
     */
    public CaseDocumentService(RestTemplate restTemplate,
                               AuthTokenGenerator authTokenGenerator,
                               @Value("${case_document_am.url}/cases/documents")
                                   String caseDocApiUrl,
                               @Value("${case_document_am.max_retries}") Integer maxApiRetries) {
        this.restTemplate = restTemplate;
        this.authTokenGenerator = authTokenGenerator;
        this.caseDocApiUrl = caseDocApiUrl;
        this.maxApiRetries = maxApiRetries;
    }

    /**
     * Given a file to upload, this will upload the file to the CCD document API and give back a unique URL to access
     * the uploaded file.
     *
     * @param authToken  the caller's bearer token used to verify the caller
     * @param caseTypeId the area the file belongs to e.g. ET_EnglandWales
     * @param file       the file to be uploaded
     * @return the URL of the document we have just uploaded
     * @throws CaseDocumentException if a problem occurs whilst uploading the document via API
     */
    public CaseDocument uploadDocument(String authToken, String caseTypeId, MultipartFile file)
        throws CaseDocumentException {
        DocumentUploadResponse response = attemptWithRetriesToUploadDocumentToCaseDocumentApi(
            0, authToken, caseTypeId, file);

        return validateResponse(
            Objects.requireNonNull(response), file.getOriginalFilename());
    }

    private DocumentUploadResponse attemptWithRetriesToUploadDocumentToCaseDocumentApi(int attempts,
                                                                               String authToken,
                                                                               String caseTypeId,
                                                                               MultipartFile file)
        throws CaseDocumentException {
        try {
            return uploadDocumentToCaseDocumentApi(authToken, caseTypeId, file).getBody();
        } catch (IOException | RestClientException e) {
            if (attempts < maxApiRetries) {
                return attemptWithRetriesToUploadDocumentToCaseDocumentApi(
                    attempts + 1, authToken, caseTypeId, file);
            }
            throw new CaseDocumentException("Failed to upload Case Document", e);
        }
    }

    private ResponseEntity<DocumentUploadResponse> uploadDocumentToCaseDocumentApi(String authToken,
                                                           String caseTypeId,
                                                           MultipartFile file)
        throws IOException, CaseDocumentException {
        validateFile(file);

        MultiValueMap<String, Object> body = generateUploadRequest(caseTypeId, file);

        HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(body, getHttpHeaders(authToken));

        return restTemplate.exchange(
            caseDocApiUrl,
            HttpMethod.POST,
            request,
            DocumentUploadResponse.class
        );
    }

    private HttpHeaders getHttpHeaders(String authToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.AUTHORIZATION, authToken);
        headers.add(SERVICE_AUTHORIZATION, authTokenGenerator.generate());
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        return headers;
    }

    private void validateFile(MultipartFile file) throws CaseDocumentException, IOException {
        String filename = file.getOriginalFilename();

        assert filename != null;
        Matcher matcher = FILE_NAME_PATTERN.matcher(filename);
        if (!matcher.matches()) {
            throw new CaseDocumentException(VALIDATE_FILE_EXCEPTION_MESSAGE);
        }

        Tika tika = new Tika();
        String detectedType = tika.detect(file.getBytes());

        if (!detectedType.equals(file.getContentType())) {
            throw new CaseDocumentException(VALIDATE_FILE_EXCEPTION_MESSAGE);
        }
    }

    private CaseDocument validateResponse(DocumentUploadResponse response, String originalFilename)
        throws CaseDocumentException {
        if (response.getDocuments() == null) {
            throw new CaseDocumentException(UPLOAD_FILE_EXCEPTION_MESSAGE + originalFilename);
        }

        CaseDocument document = response.getDocuments().stream()
            .findFirst()
            .orElseThrow(() -> new CaseDocumentException(UPLOAD_FILE_EXCEPTION_MESSAGE + originalFilename));

        if (!document.verifyUri()) {
            throw new CaseDocumentException(UPLOAD_FILE_EXCEPTION_MESSAGE + originalFilename);
        }

        return document;
    }

    private MultiValueMap<String, Object> generateUploadRequest(String caseTypeId,
                                                                MultipartFile file) throws IOException {
        ByteArrayResource fileAsResource = new ByteArrayResource(file.getBytes()) {
            @Override
            public String getFilename() {
                return file.getOriginalFilename();
            }
        };

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("files", fileAsResource);
        body.add("classification", PUBLIC.toString());
        body.add("caseTypeId", caseTypeId);
        body.add("jurisdictionId", JURISDICTION_ID);

        return body;
    }

    @Data
    private static class DocumentUploadResponse {
        private List<CaseDocument> documents;

        /**
         * Accessor for revealing if the response has any documents.
         *
         * @return a boolean that is false if documents list is empty
         */
        public Boolean isEmpty() {
            return documents.isEmpty();
        }
    }
}
