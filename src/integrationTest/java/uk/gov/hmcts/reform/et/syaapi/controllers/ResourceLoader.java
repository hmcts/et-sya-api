package uk.gov.hmcts.reform.et.syaapi.controllers;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;


public final class ResourceLoader {

    private final ObjectMapper objectMapper;

    public ResourceLoader(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public <T> T fromString(String jsonFileName, Class<T> clazz) throws IOException {
        String json = resourceAsString(jsonFileName);
        return objectMapper.readValue(json, clazz);
    }

    public String toJson(Object input) {
        try {
            return objectMapper.writeValueAsString(input);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(
                String.format("Failed to serialize '%s' to JSON", input.getClass().getSimpleName()), e
            );
        }
    }

    private static String resourceAsString(final String resourcePath) throws IOException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        final File file = ResourceUtils.getFile(classLoader.getResource(resourcePath).getFile());
        return new String(Files.readAllBytes(file.toPath()));
    }

}
