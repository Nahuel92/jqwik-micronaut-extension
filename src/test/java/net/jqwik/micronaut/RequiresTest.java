package net.jqwik.micronaut;

import io.micronaut.context.annotation.Requires;
import net.jqwik.api.Property;
import net.jqwik.micronaut.annotation.JqwikMicronautTest;

import static org.assertj.core.api.Assertions.fail;

@JqwikMicronautTest
@Requires(property = "does.not.exist")
class RequiresTest {
    @Property
    void testNotExecuted() {
        fail("Should never be executed");
    }
}