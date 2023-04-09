package net.jqwik.micronaut.hook;

import io.micronaut.test.context.TestContext;
import jakarta.annotation.Nonnull;
import net.jqwik.api.NonNullApi;
import net.jqwik.api.lifecycle.AroundTryHook;
import net.jqwik.api.lifecycle.TryExecutionResult;
import net.jqwik.api.lifecycle.TryExecutor;
import net.jqwik.api.lifecycle.TryLifecycleContext;
import net.jqwik.micronaut.extension.JqwikMicronautExtension;

import java.util.List;

public class TestInterceptor implements AroundTryHook {
    private final JqwikMicronautExtension extension;

    TestInterceptor() {
        this.extension = JqwikMicronautExtension.STORE.get();
    }

    @Override
    @NonNullApi
    @Nonnull
    public TryExecutionResult aroundTry(final TryLifecycleContext context, final TryExecutor aTry,
                                        final List<Object> parameters) throws Throwable {
        final TestContext testContext = extension.testContext(context);
        extension.interceptTest(extension.getTestMethodInvocationContext(testContext));
        return aTry.execute(parameters);
    }

    @Override
    public int aroundTryProximity() {
        return -5;
    }
}