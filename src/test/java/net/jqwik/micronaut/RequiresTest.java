package net.jqwik.micronaut;

import io.micronaut.context.annotation.Requires;
import net.jqwik.api.Property;
import net.jqwik.micronaut.annotation.JqwikMicronautTest;

import static org.assertj.core.api.Assertions.fail;

// FIXME: This test fails (the required property doesn't exist and, thus, the test class shouldn't be instantiated).
@JqwikMicronautTest
@Requires(property = "does.not.exist")
class RequiresTest {
    @Property
    void testNotExecuted() {
        fail("Should never be executed");
    }
}