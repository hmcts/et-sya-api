package uk.gov.hmcts.reform.et.syaapi.service.pdf;

import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.Arrays;


/*
 *<p>
 * Trivial implementation of the {@link MultipartFile} interface to wrap a byte[] decoded
 * from a PDF encoded String
 *</p>
 */
public class PdfDecodedMultipartFile implements MultipartFile {
    private final byte[] fileContent;

    private final String originalName;

    private final String contentType;

    public PdfDecodedMultipartFile(byte[] fileContent, String originalName, String contentType) {
        this.fileContent = Arrays.copyOf(fileContent, fileContent.length);
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
        return fileContent.length;
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
    public void transferTo(File dest) {

    }

}
