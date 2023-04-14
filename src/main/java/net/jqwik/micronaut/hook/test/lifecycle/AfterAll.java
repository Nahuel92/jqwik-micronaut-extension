package net.jqwik.micronaut.hook.test.lifecycle;

import io.micronaut.test.context.TestContext;
import net.jqwik.api.NonNullApi;
import net.jqwik.api.lifecycle.AfterContainerHook;
import net.jqwik.api.lifecycle.ContainerLifecycleContext;
import net.jqwik.micronaut.extension.JqwikMicronautExtension;

public class AfterAll implements AfterContainerHook {
    private final JqwikMicronautExtension extension;

    AfterAll() {
        this.extension = JqwikMicronautExtension.STORE.get();
    }

    @Override
    @NonNullApi
    public void afterContainer(final ContainerLifecycleContext context) throws Throwable {
        afterAll(context);
    }

    private void afterAll(final ContainerLifecycleContext context) throws Exception {
        System.out.println("9. afterAll");
        extension.afterTestClass(buildContext(context));
        extension.afterClass(context);
    }

    @Override
    public int afterContainerProximity() {
        // Run it before @BeforeContainer and after @AfterContainer
        return -20;
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
