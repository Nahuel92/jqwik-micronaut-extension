package net.jqwik.micronaut;

import io.micronaut.runtime.EmbeddedApplication;
import jakarta.inject.Inject;
import net.jqwik.api.Property;
import net.jqwik.micronaut.annotation.JqwikMicronautTest;

import static org.assertj.core.api.Assertions.assertThat;

@JqwikMicronautTest
class JqwikMicronautExtensionTest {
    @Inject
    EmbeddedApplication<?> application;

    @Property(tries = 1)
    void testItWorks() {
        assertThat(application.isRunning()).isTrue();
    }
}