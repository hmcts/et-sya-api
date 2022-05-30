package uk.gov.hmcts.reform.et.syaapi.service;

import lombok.EqualsAndHashCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import uk.gov.hmcts.ecm.common.exceptions.DocumentManagementException;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.document.am.feign.CaseDocumentClient;
import uk.gov.hmcts.reform.ccd.document.am.model.Classification;
import uk.gov.hmcts.reform.ccd.document.am.model.UploadResponse;
import uk.gov.hmcts.reform.et.syaapi.utils.ResourceLoader;

import java.net.URI;

import static java.util.Collections.singletonList;
import static junit.framework.TestCase.assertEquals;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.et.syaapi.utils.TestConstants.TEST_SERVICE_AUTH_TOKEN;

@EqualsAndHashCode
@ExtendWith(MockitoExtension.class)
public class CaseDocumentServiceTest {

    private static final String CASE_TYPE = "ET_EnglandWales";
    private static final String JURISDICTION_ID = "EMPLOYMENT";
    private static final String MOCK_TOKEN = "Bearer Token";

    private static final String MOCK_HREF = "http://dm-store:8080/documents/ee26acd5-3e51-48d5-9aa4-126a033be9ee";

    private static final MockMultipartFile MOCK_FILE = new MockMultipartFile(
        "file",
        "hello.txt",
        MediaType.TEXT_PLAIN_VALUE,
        "Hello, World!".getBytes()
      );

    private final UploadResponse requestCaseDocumentUpload = ResourceLoader.fromString(
        "responses/caseDocumentUploaded.json",
        UploadResponse.class
    );
    @Mock
    private AuthTokenGenerator authTokenGenerator;
    @Mock
    private CaseDocumentClient caseDocumentClient;



    @InjectMocks
    private CaseDocumentService caseService;

    CaseDocumentServiceTest() {

    }

    @Test
    void shouldUploadDocument() {
        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(caseDocumentClient.uploadDocuments(MOCK_TOKEN, authTokenGenerator.generate(),
                                                CASE_TYPE, JURISDICTION_ID,
                                                singletonList(MOCK_FILE),
                                                Classification.PUBLIC))
            .thenReturn(requestCaseDocumentUpload);

        URI documentEndpoint = caseService.uploadDocument(MOCK_TOKEN, CASE_TYPE, MOCK_FILE);

        assertEquals(MOCK_HREF, documentEndpoint.toString());
    }

    @Test
    void shouldThrowExceptionWhe() throws DocumentManagementException {
        Throwable documentException = new DocumentManagementException("Document to be uploaded cannot be null");

        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(caseDocumentClient.uploadDocuments(MOCK_TOKEN, authTokenGenerator.generate(),
                                                CASE_TYPE, JURISDICTION_ID,
                                                singletonList(MOCK_FILE),
                                                Classification.PUBLIC))
            .thenReturn(requestCaseDocumentEmpty);

        URI documentEndpoint = caseService.uploadDocument(MOCK_TOKEN, CASE_TYPE, MOCK_FILE);
        assertThatThrownBy(() -> caseService.uploadDocument(MOCK_TOKEN, CASE_TYPE, MOCK_FILE))
            .hasCause(documentException);
    }
}
