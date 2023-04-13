package net.jqwik.micronaut.hook;

import io.micronaut.test.context.TestContext;
import net.jqwik.api.NonNullApi;
import net.jqwik.api.lifecycle.AfterContainerHook;
import net.jqwik.api.lifecycle.ContainerLifecycleContext;
import net.jqwik.micronaut.extension.JqwikMicronautExtension;

public class AfterMicronautContainer implements AfterContainerHook {
    private final JqwikMicronautExtension extension;

    AfterMicronautContainer() {
        this.extension = JqwikMicronautExtension.STORE.get();
    }

    @Override
    @NonNullApi
    public void afterContainer(final ContainerLifecycleContext context) throws Throwable {
        extension.afterTestClass(buildContext(context));
        extension.afterClass(context);
    }

    private TestContext buildContext(final ContainerLifecycleContext context) {
        return new TestContext(
                JqwikMicronautExtension.STORE.get().getApplicationContext(),
                context.optionalContainerClass().orElse(null),
                context.optionalElement().orElse(null),
                null,
                null
        );
    }
}
