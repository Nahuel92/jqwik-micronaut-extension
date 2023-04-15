package net.jqwik.micronaut.hook.test.lifecycle;

import io.micronaut.test.annotation.MicronautTestValue;
import io.micronaut.test.context.TestContext;
import net.jqwik.api.NonNullApi;
import net.jqwik.api.lifecycle.*;
import net.jqwik.engine.support.JqwikAnnotationSupport;
import net.jqwik.micronaut.annotation.JqwikMicronautTest;
import net.jqwik.micronaut.extension.JqwikMicronautExtension;

public class BeforeAll implements BeforeContainerHook {
    private final JqwikMicronautExtension extension;

    BeforeAll() {
        this.extension = JqwikMicronautExtension.STORE.get();
    }

    @Override
    @NonNullApi
    public void beforeContainer(final ContainerLifecycleContext context) throws Exception {
        extension.beforeContainer(context);
    }

    @Override
    public int beforeContainerProximity() {
        // Run it before @BeforeContainer methods
        return -20;
    }

}
