package net.jqwik.micronaut;

import io.micronaut.runtime.EmbeddedApplication;
import jakarta.inject.Inject;
import net.jqwik.api.Property;
import net.jqwik.micronaut.annotation.JqwikMicronautTest;
import org.junit.jupiter.api.Assertions;

@JqwikMicronautTest
class JqwikMicronautExtensionTest {
    @Inject
    EmbeddedApplication<?> application;

    @Property(tries = 1)
    void testItWorks() {
        Assertions.assertTrue(application.isRunning());
    }

}
