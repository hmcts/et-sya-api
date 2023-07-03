package uk.gov.hmcts.reform.et.syaapi.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.et.syaapi.model.TornadoDocument;

import java.util.Locale;

/**
 * This is a service to generate documents given relevant data and the necessary template.
 * <p/>
 * <b><i>Note that Docmosis is an instance of Tornado running in an environment.</i></b>
 * <p/>
 * This service relies upon Docmosis as it's engine to generate required documents.
 * <p/>
 * This relies upon the following configurations to be set at an environment level:
 * <ul>
 *     <li>TORNADO_URL</li>
 *     <li>TORNADO_ACCESS_KEY</li>
 * </ul>
 * </p>
 * Docmosis is typically installed on all environments within HMCTS.  You can see more information about these
 * environments at: https://tools.hmcts.net/confluence/pages/viewpage.action?pageId=1343291506
 * </p>
 * <b>Note:</b><br/>
 * The templates are stored within the repo: https://github.com/hmcts/rdo-docmosis.
 * <br/>
 * There is a catch.  This applies to all NON-PRODUCTION environments.  The production environment follows a different
 * path, whereby the template would need to be uploaded to a sharepoint location which is documented in the page above.
 */
@Service
public class DocumentGenerationService {

    private static final String UNKNOWN_TEMPLATE_ERROR = "Unknown Template: ";
    private static final String INVALID_OUTPUT_FILE_NAME_ERROR = "Invalid output file name: ";

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final String tornadoUrl;
    private final String tornadoAccessKey;

    /**
     * Creates a new instance of {@link DocumentGenerationService} with the specified Tornado details to use its
     * service in generating the document.
     *
     * @param restTemplate     the RestTemplate to use for talking with the Tornado service
     * @param objectMapper     the {@link ObjectMapper} to generate JSON from
     * @param tornadoUrl       the Tornado URL endpoint to call to generate the document
     * @param tornadoAccessKey the access key Tornado will require for authentication
     */
    public DocumentGenerationService(RestTemplate restTemplate, ObjectMapper objectMapper,
                                     @Value("${tornado.api.url}") String tornadoUrl,
                                     @Value("${tornado.api.key}") String tornadoAccessKey) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.tornadoUrl = tornadoUrl;
        this.tornadoAccessKey = tornadoAccessKey;
    }

    /**
     * This will generate a document based upon the template name provided and the source data to populate elements
     * within the template.  The response from this will be a byte array of the PDF document.
     *
     * @param templateName   the name of the template that the Docmosis instance is aware of.
     * @param outputFileName the filename of the output document we are generating
     * @param sourceData     the {@link TornadoDocument} that contains all data to be populated in the template, in
     *                       the structure expected
     * @return a byte array of the generated document in raw format
     * @throws DocumentGenerationException should there be a problem with generating the document
     */
    public byte[] genPdfDocument(String templateName, String outputFileName, TornadoDocument sourceData)
        throws DocumentGenerationException {
        validateGenDocInputs(templateName, outputFileName, sourceData);

        try {
            return generateDocument(generateTornadoRequestWrapper(templateName, outputFileName, sourceData));
        } catch (RestClientException e) {
            throw new DocumentGenerationException("Failed to connect with Tornado", e);
        }
    }

    private void validateGenDocInputs(String templateName, String outputFileName, TornadoDocument sourceData)
        throws DocumentGenerationException {
        if (Strings.isBlank(templateName) || !templateName.toLowerCase(Locale.ROOT).endsWith(".docx")) {
            throw new DocumentGenerationException(UNKNOWN_TEMPLATE_ERROR + templateName);
        }
        if (Strings.isBlank(outputFileName) || !outputFileName.toLowerCase(Locale.ROOT).endsWith(".pdf")) {
            throw new DocumentGenerationException(INVALID_OUTPUT_FILE_NAME_ERROR + outputFileName);
        }
        if (sourceData == null) {
            throw new DocumentGenerationException("sourceData MUST NOT be null");
        }
    }

    private TornadoRequestWrapper generateTornadoRequestWrapper(String templateName, String outputFileName,
                                                                TornadoDocument sourceData) {
        TornadoRequestWrapper requestWrapper = new TornadoRequestWrapper();
        requestWrapper.setAccessKey(tornadoAccessKey);
        requestWrapper.setTemplateName(templateName);
        requestWrapper.setOutputName(outputFileName);
        requestWrapper.setData(sourceData);
        return requestWrapper;
    }

    private byte[] generateDocument(TornadoRequestWrapper requestWrapper)
        throws DocumentGenerationException {
        String body;
        try {
            body = objectMapper.writeValueAsString(requestWrapper);
        } catch (JsonProcessingException e) {
            throw new DocumentGenerationException("Failed to convert the TornadoRequestWrapper to a string", e);
        }
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json; charset=utf-8");
        HttpEntity<byte[]> request = new HttpEntity<>(body.getBytes(), headers);
        ResponseEntity<byte[]> response;
        response = this.restTemplate.exchange(tornadoUrl, HttpMethod.POST, request, byte[].class);
        return response.getBody();
    }

    @Data
    private static class TornadoRequestWrapper {
        String accessKey;
        String templateName;
        String outputName;
        TornadoDocument data;
    }
}
