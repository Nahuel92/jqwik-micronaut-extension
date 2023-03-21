package net.jqwik.micronaut;

import io.micronaut.context.annotation.Value;
import io.micronaut.runtime.EmbeddedApplication;
import io.micronaut.test.support.TestPropertyProvider;
import jakarta.inject.Inject;
import net.jqwik.api.Property;
import net.jqwik.micronaut.annotation.JqwikMicronautTest;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JqwikMicronautTest
@io.micronaut.context.annotation.Property(name = "test.class.property", value = "Hello world!")
class ApplicationPropertyTest implements TestPropertyProvider {
    @Inject
    private EmbeddedApplication<?> application;

    @Value("${main.property}")
    private String mainProperty;

    @Value("${test.property}")
    private String testProperty;

    @Value("${test.class.property}")
    private String classProperty;

    @Value("${dynamic.property}")
    private String dynamicProperty;

    @Override
    public Map<String, String> getProperties() {
        return Map.ofEntries(Map.entry("dynamic.property", "value"));
    }

    @Property
    @io.micronaut.context.annotation.Property(name = "test.method.property", value = "Hello method!")
    void successOnInjectingApplicationProperties() {
        assertThat(mainProperty).isEqualTo("Hello");
        assertThat(testProperty).isEqualTo("world!");
        assertThat(classProperty).isEqualTo("Hello world!");
        assertThat(getTestProperty()).contains("Hello method!");
        assertThat(dynamicProperty).isEqualTo("value");
    }

    private Optional<String> getTestProperty() {
        return application.getApplicationContext().getProperty("test.method.property", String.class);
    }
}
