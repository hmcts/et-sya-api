package uk.gov.hmcts.reform.et.syaapi.consumer.document;

import au.com.dius.pact.consumer.MockServer;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import au.com.dius.pact.core.model.annotations.PactFolder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.fluent.Executor;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.et.syaapi.models.ClaimCaseDocument;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.http.HttpMethod.POST;

@ExtendWith(PactConsumerTestExt.class)
@ExtendWith(SpringExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@PactTestFor(providerName = "etSyaService_pdfgenerationEndpoint", port = "8891")
@PactFolder("pacts")
class PdfGenerationServiceConsumerTest {
    private static final String SERVICE_AUTHORIZATION_HEADER = "ServiceAuthorization";
    ObjectMapper objectMapper;
    private static final String PDF_URL = "/pdfs";
    private static final String SOME_SERVICE_AUTH_TOKEN = "someServiceAuthToken";

    @BeforeEach
    void setUpEachTest() throws InterruptedException {
        Thread.sleep(2000);
        objectMapper = new ObjectMapper();
    }

    @AfterEach
    void teardown() {
        Executor.closeIdleConnections();
    }

    @Pact(provider = "etsyaService_pdfgenerationEndpoint", consumer = "et_sya_api_service")
    RequestResponsePact generatePdfFromTemplate(PactDslWithProvider builder) throws JsonProcessingException {
        return builder
            .given("A request to generate a pdf document")
            .uponReceiving("a request to generate a pdf document with a template")
            .method(POST.toString())
            .headers(SERVICE_AUTHORIZATION_HEADER, SOME_SERVICE_AUTH_TOKEN)
            .body(createJsonObject(createClaimCase()),
                  "application/vnd.uk.gov.hmcts.pdf-service.v2+json;charset=UTF-8")
            .path(PDF_URL)
            .willRespondWith()
            .withBinaryData("".getBytes(), "application/octet-stream")
            .matchHeader(org.springframework.http.HttpHeaders.CONTENT_TYPE,
                         "application/pdf")
            .status(200)
            .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "generatePdfFromTemplate")
    void verifyGeneratePdfFromTemplatePact(MockServer mockServer) throws IOException {
        HttpClient httpClient = HttpClientBuilder.create().build();

        HttpPost request = new HttpPost(mockServer.getUrl() + PDF_URL);
        StringEntity json = new StringEntity(createJsonObject(createClaimCase()));
        request.addHeader(SERVICE_AUTHORIZATION_HEADER, SOME_SERVICE_AUTH_TOKEN);
        request.addHeader("content-type", "application/vnd.uk.gov.hmcts.pdf-service.v2+json;charset=UTF-8");
        request.setEntity(json);

        HttpResponse generateDocumentResponse = httpClient.execute(request);
        String responseContentType = generateDocumentResponse.getEntity().getContentType().toString();

        assertEquals(200, generateDocumentResponse.getStatusLine().getStatusCode());
        assertEquals("Content-type: application/pdf", responseContentType);
    }

    protected String createJsonObject(Object obj) throws JsonProcessingException {
        return objectMapper.writeValueAsString(obj);
    }

    private ClaimCaseDocument createClaimCase() {
        ClaimCaseDocument claimCaseDocument = new ClaimCaseDocument();
        claimCaseDocument.setTestMessage("Hello World");
        return claimCaseDocument;
    }
}
