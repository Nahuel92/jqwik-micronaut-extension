package net.jqwik.micronaut.hook;

import io.micronaut.test.context.TestContext;
import io.micronaut.test.context.TestMethodInvocationContext;
import jakarta.annotation.Nonnull;
import net.jqwik.api.NonNullApi;
import net.jqwik.api.lifecycle.TryExecutionResult;
import net.jqwik.api.lifecycle.TryExecutor;
import net.jqwik.api.lifecycle.TryLifecycleContext;
import net.jqwik.micronaut.extension.JqwikMicronautExtension;

import java.util.List;

public class AroundTryHook implements net.jqwik.api.lifecycle.AroundTryHook {
    @Override
    @NonNullApi
    @Nonnull
    public TryExecutionResult aroundTry(final TryLifecycleContext context,
                                        final TryExecutor aTry, final List<Object> parameters) throws Throwable {
        final TestContext testContext = buildContext(context);
        JqwikMicronautExtension.EXTENSION_STORE.get().interceptTest(new TestMethodInvocationContext<>() {
            @Override
            public TestContext getTestContext() {
                return testContext;
            }

            @Override
            public Object proceed() {
                return aTry.execute(parameters);
            }
        });
        return aTry.execute(parameters);
    }

    @Override
    public int aroundTryProximity() {
        return -15;
    }

    private TestContext buildContext(final TryLifecycleContext context) {
        return new TestContext(
                JqwikMicronautExtension.EXTENSION_STORE.get().getApplicationContext(),
                context.containerClass(),
                context.targetMethod(),
                context.testInstance(),
                null
        );
    }
}
