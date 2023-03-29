package net.jqwik.micronaut;

import net.jqwik.api.Property;
import net.jqwik.micronaut.annotation.JqwikMicronautTest;

import static org.assertj.core.api.Assertions.assertThat;

// FIXME: This test fails to pass (the file is not found by Micronaut, leading to a "property not found" error)
@JqwikMicronautTest(propertySources = "myprops.properties")
class PropertySourceTest {
    @io.micronaut.context.annotation.Property(name = "foo.bar")
    private String val;

    @Property
    void testPropertySource() {
        assertThat(val).isEqualTo("foo");
    }
}