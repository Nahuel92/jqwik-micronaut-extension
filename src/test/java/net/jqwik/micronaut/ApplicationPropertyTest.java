package net.jqwik.micronaut;

import io.micronaut.context.annotation.Value;
import io.micronaut.test.support.TestPropertyProvider;
import net.jqwik.api.Property;
import net.jqwik.micronaut.annotation.JqwikMicronautTest;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@JqwikMicronautTest
class ApplicationPropertyTest implements TestPropertyProvider {
    @Value("${main.property}")
    private String mainProperty;

    @Value("${test.property}")
    private String testProperty;

    @Value("${dynamic.property}")
    private String dynamicProperty;

    @Override
    public Map<String, String> getProperties() {
        return Map.ofEntries(Map.entry("dynamic.property", "value"));
    }

    @Property
    void successOnInjectingApplicationProperties() {
        assertThat(mainProperty).isEqualTo("Hello");
        assertThat(testProperty).isEqualTo("world!");
        assertThat(dynamicProperty).isEqualTo("value");
    }
}
