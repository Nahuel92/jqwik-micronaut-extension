package net.jqwik.micronaut;

import net.jqwik.api.Property;
import net.jqwik.api.lifecycle.BeforeProperty;
import net.jqwik.micronaut.annotation.JqwikMicronautTest;

import static org.assertj.core.api.Assertions.assertThat;

@JqwikMicronautTest
class ConstructorInjectionTest {
    private final AppBean appBean;

    ConstructorInjectionTest(final AppBean appBean) {
        this.appBean = appBean;
    }

    @BeforeProperty
    void injectStatic(final AppBean appBean) {
        assertThat(appBean).isNotNull();
    }

    @Property
    void testConstructorInjected() {
        assertThat(appBean).isNotNull();
    }
}
