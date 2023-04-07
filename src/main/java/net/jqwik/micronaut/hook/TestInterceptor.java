package net.jqwik.micronaut.hook;

import jakarta.annotation.Nonnull;
import net.jqwik.api.NonNullApi;
import net.jqwik.api.lifecycle.AroundTryHook;
import net.jqwik.api.lifecycle.TryExecutionResult;
import net.jqwik.api.lifecycle.TryExecutor;
import net.jqwik.api.lifecycle.TryLifecycleContext;
import net.jqwik.micronaut.extension.JqwikMicronautExtension;

import java.util.List;

public class TestInterceptor implements AroundTryHook {
    @Override
    @NonNullApi
    @Nonnull
    public TryExecutionResult aroundTry(final TryLifecycleContext context, final TryExecutor aTry,
                                        final List<Object> parameters) throws Throwable {
        final var testContext = JqwikMicronautExtension.EXTENSION_STORE.get().testContext(context);
        JqwikMicronautExtension.EXTENSION_STORE.get().interceptTest(
                JqwikMicronautExtension.EXTENSION_STORE.get().getTestMethodInvocationContext(testContext)
        );
        return aTry.execute(parameters);
    }

    @Override
    public int aroundTryProximity() {
        return -5;
    }
}
