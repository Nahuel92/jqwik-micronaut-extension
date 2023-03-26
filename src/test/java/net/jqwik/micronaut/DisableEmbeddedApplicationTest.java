package net.jqwik.micronaut;

import io.micronaut.runtime.EmbeddedApplication;
import jakarta.inject.Inject;
import net.jqwik.api.Property;
import net.jqwik.micronaut.annotation.JqwikMicronautTest;

import static org.assertj.core.api.Assertions.assertThat;

@JqwikMicronautTest(startApplication = false, rebuildContext = true)
class DisableEmbeddedApplicationTest {
    @Inject
    private EmbeddedApplication<?> embeddedApplication;

    @Property
    void embeddedApplicationIsNotStartedWhenContextIsStarted() {
        assertThat(embeddedApplication.isRunning()).isFalse();
    }

    @Property
    void embeddedApplicationIsNotStartedWhenContextIsRebuilt() {
        assertThat(embeddedApplication.isRunning()).isFalse();
    }
}
