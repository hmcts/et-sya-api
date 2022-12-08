package uk.gov.hmcts.reform.et.syaapi.service;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.et.common.model.ccd.items.DocumentTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.DocumentType;
import uk.gov.hmcts.et.common.model.ccd.types.UploadedDocumentType;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.et.syaapi.config.interceptors.ResourceNotFoundException;
import uk.gov.hmcts.reform.et.syaapi.models.CaseDocument;
import uk.gov.hmcts.reform.et.syaapi.service.pdf.PdfDecodedMultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.springframework.http.HttpStatus.NOT_FOUND;
import static uk.gov.hmcts.reform.ccd.client.model.Classification.PUBLIC;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.JURISDICTION_ID;
import static uk.gov.hmcts.reform.et.syaapi.constants.EtSyaConstants.RESOURCE_NOT_FOUND;

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
@SuppressWarnings("PMD.TooManyMethods")
public class CaseDocumentService {
    private static final String SERVICE_AUTHORIZATION = "ServiceAuthorization";
    private static final String FILE_NAME_REGEX_PATTERN =
        "^(?!\\.)[^\\|*\\?\\:<>\\/$\"]{1,150}$";
    private static final Pattern FILE_NAME_PATTERN = Pattern.compile(FILE_NAME_REGEX_PATTERN);
    private static final String UPLOAD_FILE_EXCEPTION_MESSAGE = "Document management failed uploading file: ";
    private static final String VALIDATE_FILE_EXCEPTION_MESSAGE = "File does not pass validation";
    private static final String TYPE_OF_DOCUMENT_ET1_CASE_PDF = "ET1";
    private static final String TYPE_OF_DOCUMENT_ET1_ACAS_CERTIFICATE = "ACAS Certificate";
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
                               @Value("${case_document_am.url}")
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

    /**
     * Returns content in binary stream of the given document id.
     *
     * @param authToken  the caller's bearer token used to verify the caller
     * @param documentId the id of the document
     * @return the response entity which contains the binary stream
     * @throws ResourceNotFoundException if the target API returns 404 response code
     */
    public ResponseEntity<ByteArrayResource> downloadDocument(String authToken, UUID documentId) {
        log.info("Called downloadDocument");
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.AUTHORIZATION, authToken);
        headers.add(SERVICE_AUTHORIZATION, authTokenGenerator.generate());
        HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(headers);

        try {
            return restTemplate.exchange(
                caseDocApiUrl + "/cases/documents/" + documentId + "/binary",
                HttpMethod.GET,
                request,
                ByteArrayResource.class
            );
        } catch (HttpClientErrorException ex) {
            if (NOT_FOUND.equals(ex.getStatusCode())) {
                throw new ResourceNotFoundException(String.format(RESOURCE_NOT_FOUND,
                                                                  documentId, ex.getMessage()), ex);
            }
            throw ex;
        }

    }

    /**
     * Returns document details of the given document id.
     *
     * @param authToken  the caller's bearer token used to verify the caller
     * @param documentId the id of the document
     * @return the response entity which contains the document details object
     * @throws ResourceNotFoundException if the target API returns 404 response code
     */
    public ResponseEntity<CaseDocument> getDocumentDetails(String authToken, UUID documentId) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.AUTHORIZATION, authToken);
        headers.add(SERVICE_AUTHORIZATION, authTokenGenerator.generate());
        HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(headers);
        try {
            ResponseEntity<CaseDocument> response = restTemplate.exchange(
                caseDocApiUrl + "/cases/documents/" + documentId,
                HttpMethod.GET,
                request,
                CaseDocument.class
            );

            return new ResponseEntity<>(response.getBody(), getResponseHeaders(), HttpStatus.OK);
        } catch (HttpClientErrorException ex) {
            if (NOT_FOUND.equals(ex.getStatusCode())) {
                throw new ResourceNotFoundException(String.format(RESOURCE_NOT_FOUND,
                                                                  documentId, ex.getMessage()), ex);
            }
            throw ex;
        }
    }

    private HttpHeaders getResponseHeaders() {
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.add("Connection", "keep-alive");
        responseHeaders.add("Content-Type", "application/json");
        responseHeaders.add("X-Frame-Options", "DENY");
        responseHeaders.add("X-XSS-Protection", "1; mode=block");
        responseHeaders.add("X-Content-Type-Options", "nosniff");

        return responseHeaders;
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
            caseDocApiUrl + "/cases/documents",
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

    /**
     * Accepts all files for a given case as a {@link List<PdfDecodedMultipartFile>} and uploads them.
     * Files are uploaded one at a file via this service and then returned as a list of {@link DocumentTypeItem}
     * @param authToken jwt token used to call this service
     * @param caseType defines the juridiction of the case e.g. ET_EnglandWales
     * @param pdfDecodedMultipartFiles The pdf files that are generated for the case upon submittion
     * @param acasCertificates The acas certificates that are converted to pdf format for a case
     * @return a complete list of each successfully uploaded file passed to the function
     * @throws CaseDocumentException thrown if there is an error encounted whilst uploading a file
     */
    public List<DocumentTypeItem> uploadAllDocuments(String authToken,
                                                     String caseType,
                                                     List<PdfDecodedMultipartFile> pdfDecodedMultipartFiles,
                                                     List<PdfDecodedMultipartFile> acasCertificates)
        throws CaseDocumentException {
        List<DocumentTypeItem> documentTypeItems = new ArrayList<>();
        if (pdfDecodedMultipartFiles != null) {
            for (PdfDecodedMultipartFile casePdf : pdfDecodedMultipartFiles) {
                documentTypeItems.add(createDocumentTypeItem(
                    authToken,
                    caseType,
                    TYPE_OF_DOCUMENT_ET1_CASE_PDF,
                    casePdf
                ));
            }
        }
        if (acasCertificates != null) {
            for (PdfDecodedMultipartFile acasCertificate : acasCertificates) {
                documentTypeItems.add(createDocumentTypeItem(
                    authToken,
                    caseType,
                    TYPE_OF_DOCUMENT_ET1_ACAS_CERTIFICATE,
                    acasCertificate
                ));
            }
        }
        return documentTypeItems;
    }

    /**
     * Accepts a {@link UploadedDocumentType} and wraps it in a {@link DocumentTypeItem} and assigns a randon UUID.
     * @param typeOfDocument specifies the relevance of the document to the case
     * @param uploadedDoc is to be wrapped and returned
     * @return a {@link DocumentTypeItem} with the document and a new UUID
     */
    public DocumentTypeItem createDocumentTypeItem(String typeOfDocument, UploadedDocumentType uploadedDoc) {
        DocumentTypeItem documentTypeItem = new DocumentTypeItem();
        documentTypeItem.setId(UUID.randomUUID().toString());

        DocumentType documentType = new DocumentType();
        documentType.setTypeOfDocument(typeOfDocument);
        documentType.setUploadedDocument(uploadedDoc);

        documentTypeItem.setValue(documentType);

        return documentTypeItem;
    }

    private DocumentTypeItem createDocumentTypeItem(String authToken,
                                                    String caseType,
                                                    String documentType,
                                                    PdfDecodedMultipartFile pdfDecodedMultipartFile)
        throws CaseDocumentException {
        CaseDocument caseDocument = uploadDocument(authToken, caseType, pdfDecodedMultipartFile);
        return createDocumentTypeItemFromCaseDocument(caseDocument, documentType,
                                                      pdfDecodedMultipartFile.getDocumentDescription());
    }

    private DocumentTypeItem createDocumentTypeItemFromCaseDocument(CaseDocument caseDocument,
                                                                   String typeOfDocument,
                                                                   String shortDescription) {
        DocumentType documentType = new DocumentType();
        documentType.setTypeOfDocument(typeOfDocument);
        documentType.setShortDescription(shortDescription);
        UploadedDocumentType uploadedDocumentType = new UploadedDocumentType();
        uploadedDocumentType.setDocumentFilename(caseDocument.getOriginalDocumentName());
        uploadedDocumentType.setDocumentUrl(caseDocument.getLinks().get("self").get("href"));
        uploadedDocumentType.setDocumentBinaryUrl(caseDocument.getLinks().get("binary") == null ? null :
                                                      caseDocument.getLinks().get("binary").get("href"));
        documentType.setUploadedDocument(uploadedDocumentType);
        DocumentTypeItem documentTypeItem = new DocumentTypeItem();
        documentTypeItem.setId(UUID.randomUUID().toString());
        documentTypeItem.setValue(documentType);
        return documentTypeItem;
    }
}
