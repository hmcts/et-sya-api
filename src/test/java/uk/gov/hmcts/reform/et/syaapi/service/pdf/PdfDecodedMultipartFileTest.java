package uk.gov.hmcts.reform.et.syaapi.service.pdf;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.io.File;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PdfDecodedMultipartFileTest {

    PdfDecodedMultipartFile pdfDecodedMultipartFile;

    private static final String ORIGINAL_FILE_NAME = "ET1_Michael_Jackson.pdf";
    private static final String CONTENT_TYPE = "Application/Pdf";
    private static final byte[] FILE_CONTENT = {};

    @BeforeEach
    void beforeEach() {
        pdfDecodedMultipartFile = new PdfDecodedMultipartFile(new byte[] {}, ORIGINAL_FILE_NAME, CONTENT_TYPE);
    }

    @Test
    void shouldGetNameReturnNull() {
        assertThat(pdfDecodedMultipartFile.getName()).isNull();
    }

    @Test
    void shouldGetOriginalFileNameReturnOriginalFileName() {
        assertThat(pdfDecodedMultipartFile.getOriginalFilename()).isEqualTo(ORIGINAL_FILE_NAME);
    }

    @Test
    void shouldGetContentTypeReturnContentType() {
        assertThat(pdfDecodedMultipartFile.getContentType()).isEqualTo(CONTENT_TYPE);
    }

    @Test
    void shouldIsEmptyReturnTrue() {
        assertThat(pdfDecodedMultipartFile.isEmpty()).isTrue();
    }

    @Test
    void shouldGetSizeReturnZero() {
        assertThat(pdfDecodedMultipartFile.getSize()).isZero();
    }

    @Test
    void shouldGetSizeReturnZeroWhenFileContentNull() {
        assertThat(new PdfDecodedMultipartFile(null, ORIGINAL_FILE_NAME, CONTENT_TYPE).getSize()).isZero();
    }

    @Test
    void shouldGetBytesReturnEmptyByteArray() {
        assertThat(pdfDecodedMultipartFile.getBytes()).isEqualTo(FILE_CONTENT);
    }

    @Test
    void shouldGetInputStreamReturnEmptyInputStream() throws IOException {
        assertThat(pdfDecodedMultipartFile.getInputStream().readAllBytes()).isEqualTo(FILE_CONTENT);
    }

    @Test
    void shouldTransferToHaveNoError() {
        File tmpFile = new File("dummyFile.pdf");
        assertDoesNotThrow(() -> pdfDecodedMultipartFile.transferTo(tmpFile));
        assertThat(tmpFile.delete()).isTrue();
    }
}
