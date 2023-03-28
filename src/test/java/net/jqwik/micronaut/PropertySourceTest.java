package net.jqwik.micronaut;

import net.jqwik.api.Property;
import net.jqwik.micronaut.annotation.JqwikMicronautTest;
import org.assertj.core.api.Assertions;

@JqwikMicronautTest(propertySources = "myprops.properties")
class PropertySourceTest {
    @io.micronaut.context.annotation.Property(name = "foo.bar")
    private String val;

    @Property
    void testPropertySource() {
        Assertions.assertThat(val).isEqualTo("foo");
    }
}