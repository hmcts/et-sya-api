package uk.gov.hmcts.reform.et.syaapi.config;

import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.core.io.support.PropertySourceFactory;

import java.util.Objects;
import java.util.Properties;

/**
 * Factory to enable YAML files to be used with {@link PropertySource}.
 *
 */
public class YamlPropertySourceFactory implements PropertySourceFactory {

    /**
     * Creates the property source.
     * @param name the name of the property source.
     *      (can be {@code null} in which case the factory implementation
     *      will have to generate a name based on the given resource)
     * @param resource the resource (potentially encoded) to wrap
     * @return a new {@link PropertySource}
     */
    @Override
    public PropertySource<?> createPropertySource(String name, EncodedResource resource) {
        YamlPropertiesFactoryBean factory = new YamlPropertiesFactoryBean();
        factory.setResources(resource.getResource());

        Properties properties = factory.getObject();

        return new PropertiesPropertySource(Objects.requireNonNull(resource.getResource().getFilename()),
            Objects.requireNonNull(properties));
    }
}
