package net.jqwik.micronaut.hook;

import io.micronaut.test.context.TestContext;
import jakarta.annotation.Nonnull;
import net.jqwik.api.NonNullApi;
import net.jqwik.api.lifecycle.AroundPropertyHook;
import net.jqwik.api.lifecycle.PropertyExecutionResult;
import net.jqwik.api.lifecycle.PropertyExecutor;
import net.jqwik.api.lifecycle.PropertyLifecycleContext;
import net.jqwik.micronaut.extension.JqwikMicronautExtension;

public class AroundInterceptor implements AroundPropertyHook {
    private final JqwikMicronautExtension extension;

    AroundInterceptor() {
        this.extension = JqwikMicronautExtension.STORE.get();
    }

    @Override
    @NonNullApi
    @Nonnull
    public PropertyExecutionResult aroundProperty(final PropertyLifecycleContext context,
                                                  final PropertyExecutor property) throws Throwable {
        final TestContext testContext = extension.testContext(context);
        interceptBeforeEach(testContext);
        final PropertyExecutionResult execute = property.execute();
        interceptAfterEach(testContext);
        return execute;
    }

    private void interceptBeforeEach(final TestContext testContext) throws Throwable {
        extension.beforeSetupTest(testContext);
        extension.interceptBeforeEach(extension.getTestMethodInvocationContext(testContext));
        extension.afterSetupTest(testContext);
    }

    private void interceptAfterEach(final TestContext testContext) throws Throwable {
        extension.beforeCleanupTest(testContext);
        extension.interceptAfterEach(extension.getTestMethodInvocationContext(testContext));
        extension.afterCleanupTest(testContext);
    }

    @Override
    public int aroundPropertyProximity() {
        return -15;
    }
}