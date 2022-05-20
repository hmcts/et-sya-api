package uk.gov.hmcts.reform.et.syaapi.controllers;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public final class ResourceLoader {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
        .registerModule(new Jdk8Module())
        .registerModule(new ParameterNamesModule(JsonCreator.Mode.PROPERTIES))
        .registerModule(new JavaTimeModule()).disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        .setSerializationInclusion(JsonInclude.Include.NON_EMPTY)
        .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);

    private ResourceLoader() {
        // Utility class
    }

    public static <T> T fromString(String jsonFileName, Class<T> clazz) throws IOException {
        String json = resourceAsString(jsonFileName);
        return OBJECT_MAPPER.readValue(json, clazz);
    }

    public static String toJson(Object input) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
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
