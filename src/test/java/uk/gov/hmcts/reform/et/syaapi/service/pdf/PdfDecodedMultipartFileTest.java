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
@SuppressWarnings({"PMD.TooManyMethods"})
class PdfDecodedMultipartFileTest {

    PdfDecodedMultipartFile pdfDecodedMultipartFile;

    private static final String ORIGINAL_FILE_NAME = "ET1_Michael_Jackson.pdf";
    private static final String CONTENT_TYPE = "Application/Pdf";

    public static final byte[] SAMPLE_BYTE_ARRAY = { 37, 80, 68, 70, 45, 49, 46, 49, 10, 37, -62, -91, -62, -79, -61,
        -85, 10, 10, 49, 32, 48, 32, 111, 98, 106, 10, 32, 32, 60, 60, 32, 47, 84, 121, 112, 101, 32, 47, 67, 97, 116,
        97, 108, 111, 103, 10, 32, 32, 32, 32, 32, 47, 80, 97, 103, 101, 115, 32, 50, 32, 48, 32, 82, 10, 32, 32, 62,
        62, 10, 101, 110, 100, 111, 98, 106, 10, 10, 50, 32, 48, 32, 111, 98, 106, 10, 32, 32, 60, 60, 32, 47, 84, 121,
        112, 101, 32, 47, 80, 97, 103, 101, 115, 10, 32, 32, 32, 32, 32, 47, 75, 105, 100, 115, 32, 91, 51, 32, 48, 32,
        82, 93, 10, 32, 32, 32, 32, 32, 47, 67, 111, 117, 110, 116, 32, 49, 10, 32, 32, 32, 32, 32, 47, 77, 101, 100,
        105, 97, 66, 111, 120, 32, 91, 48, 32, 48, 32, 51, 48, 48, 32, 49, 52, 52, 93, 10, 32, 32, 62, 62, 10, 101,
        110, 100, 111, 98, 106, 10, 10, 51, 32, 48, 32, 111, 98, 106, 10, 32, 32, 60, 60, 32, 32, 47, 84, 121, 112,
        101, 32, 47, 80, 97, 103, 101, 10, 32, 32, 32, 32, 32, 32, 47, 80, 97, 114, 101, 110, 116, 32, 50, 32, 48, 32,
        82, 10, 32, 32, 32, 32, 32, 32, 47, 82, 101, 115, 111, 117, 114, 99, 101, 115, 10, 32, 32, 32, 32, 32, 32, 32,
        60, 60, 32, 47, 70, 111, 110, 116, 10, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 60, 60, 32, 47, 70, 49, 10,
        32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 60, 60, 32, 47, 84, 121, 112, 101, 32, 47, 70, 111,
        110, 116, 10, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 47, 83, 117, 98, 116,
        121, 112, 101, 32, 47, 84, 121, 112, 101, 49, 10, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32,
        32, 32, 32, 47, 66, 97, 115, 101, 70, 111, 110, 116, 32, 47, 84, 105, 109, 101, 115, 45, 82, 111, 109, 97, 110,
        10, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 62, 62, 10, 32, 32, 32, 32, 32, 32, 32, 32, 32,
        32, 32, 62, 62, 10, 32, 32, 32, 32, 32, 32, 32, 62, 62, 10, 32, 32, 32, 32, 32, 32, 47, 67, 111, 110, 116, 101,
        110, 116, 115, 32, 52, 32, 48, 32, 82, 10, 32, 32, 62, 62, 10, 101, 110, 100, 111, 98, 106, 10, 10, 52, 32, 48,
        32, 111, 98, 106, 10, 32, 32, 60, 60, 32, 47, 76, 101, 110, 103, 116, 104, 32, 53, 53, 32, 62, 62, 10, 115,
        116, 114, 101, 97, 109, 10, 32, 32, 66, 84, 10, 32, 32, 32, 32, 47, 70, 49, 32, 49, 56, 32, 84, 102, 10, 32,
        32, 32, 32, 48, 32, 48, 32, 84, 100, 10, 32, 32, 32, 32, 40, 72, 101, 108, 108, 111, 32, 87, 111, 114, 108,
        100, 41, 32, 84, 106, 10, 32, 32, 69, 84, 10, 101, 110, 100, 115, 116, 114, 101, 97, 109, 10, 101, 110, 100,
        111, 98, 106, 10, 10, 120, 114, 101, 102, 10, 48, 32, 53, 10, 48, 48, 48, 48, 48, 48, 48, 48, 48, 48, 32, 54,
        53, 53, 51, 53, 32, 102, 32, 10, 48, 48, 48, 48, 48, 48, 48, 48, 49, 56, 32, 48, 48, 48, 48, 48, 32, 110, 32,
        10, 48, 48, 48, 48, 48, 48, 48, 48, 55, 55, 32, 48, 48, 48, 48, 48, 32, 110, 32, 10, 48, 48, 48, 48, 48, 48,
        48, 49, 55, 56, 32, 48, 48, 48, 48, 48, 32, 110, 32, 10, 48, 48, 48, 48, 48, 48, 48, 52, 53, 55, 32, 48, 48,
        48, 48, 48, 32, 110, 32, 10, 116, 114, 97, 105, 108, 101, 114, 10, 32, 32, 60, 60, 32, 32, 47, 82, 111, 111,
        116, 32, 49, 32, 48, 32, 82, 10, 32, 32, 32, 32, 32, 32, 47, 83, 105, 122, 101, 32, 53, 10, 32, 32, 62, 62, 10,
        115, 116, 97, 114, 116, 120, 114, 101, 102, 10, 53, 54, 53, 10, 37, 37, 69, 79, 70, 10 };
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
    void shouldIsEmptyReturnTrueWhenFileContentIsNull() {
        assertThat(new PdfDecodedMultipartFile(null, ORIGINAL_FILE_NAME, CONTENT_TYPE).isEmpty()).isTrue();
    }

    @Test
    void shouldIsEmptyReturnFalseWhenFileContentIsNotEmpty() {
        assertThat(new PdfDecodedMultipartFile(SAMPLE_BYTE_ARRAY, ORIGINAL_FILE_NAME, CONTENT_TYPE)
                       .isEmpty()).isFalse();
    }

    @Test
    void shouldGetSizeReturnZero() {
        assertThat(pdfDecodedMultipartFile.getSize()).isZero();
    }

    @Test
    void shouldGetSizeReturnZeroWhenFileContentIsNull() {
        assertThat(new PdfDecodedMultipartFile(null, ORIGINAL_FILE_NAME, CONTENT_TYPE).getSize()).isZero();
    }

    @Test
    void shouldGetBytesReturnEmptyByteArray() {
        assertThat(pdfDecodedMultipartFile.getBytes()).isEqualTo(FILE_CONTENT);
    }

    @Test
    void shouldGetBytesReturnEmptyByteArrayWhenFileContentIsNull() {
        assertThat(new PdfDecodedMultipartFile(null, ORIGINAL_FILE_NAME, CONTENT_TYPE)
                       .getBytes()).isEqualTo(FILE_CONTENT);
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
