package uk.gov.hmcts.reform.et.syaapi.service.pdf;

import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Arrays;

/*
 *<p>
 * Trivial implementation of the {@link MultipartFile} interface to wrap a byte[] decoded
 * from a PDF encoded String
 *</p>
 */
@SuppressWarnings({"NullableProblems", "resource"})
public class PdfDecodedMultipartFile implements MultipartFile {
    private final byte[] fileContent;

    private final String originalName;

    private final String contentType;

    public PdfDecodedMultipartFile(byte[] fileContent, String originalName, String contentType) {
        this.fileContent = fileContent == null ? null : Arrays.copyOf(fileContent, fileContent.length);
        this.originalName = originalName;
        this.contentType = contentType;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public String getOriginalFilename() {
        return originalName;
    }

    @Override
    public String getContentType() {
        return this.contentType;
    }

    @Override
    public boolean isEmpty() {
        return fileContent == null || fileContent.length == 0;
    }

    @Override
    public long getSize() {
        return fileContent == null ? 0 : fileContent.length;
    }

    @Override
    public byte[] getBytes() {
        if (fileContent == null) {
            return new byte[0];
        } else {
            return Arrays.copyOf(fileContent, fileContent.length);
        }
    }

    @Override
    public InputStream getInputStream() {
        return new ByteArrayInputStream(fileContent);
    }

    @Override
    public void transferTo(File dest) throws IOException {
        Files.newOutputStream(dest.toPath()).write(fileContent);
    }
}
