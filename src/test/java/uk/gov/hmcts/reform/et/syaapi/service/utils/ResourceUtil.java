package uk.gov.hmcts.reform.et.syaapi.service.utils;

import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public final class ResourceUtil {

    private ResourceUtil() {
    }

    public static String expectedResponse(final String resourcePath) throws IOException {
        return resourceAsString(resourcePath);
    }

    public static String resourceAsString(final String resourcePath) throws IOException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        final File file = ResourceUtils.getFile(classLoader.getResource(resourcePath).getFile());
        return new String(Files.readAllBytes(file.toPath()));
    }

    public static byte[] resourceAsBytes(final String resourcePath) throws IOException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        final File file = ResourceUtils.getFile(classLoader.getResource(resourcePath).getFile());
        return Files.readAllBytes(file.toPath());
    }
}
