package net.jqwik.micronaut.hook;

import io.micronaut.test.context.TestContext;
import io.micronaut.test.context.TestMethodInvocationContext;
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
        interceptTestMethod(context, aTry, parameters);
        return aTry.execute(parameters);
    }

    private void interceptTestMethod(final TryLifecycleContext context, final TryExecutor aTry,
                                     final List<Object> parameters) throws Throwable {
        extension.interceptTest(
                new TestMethodInvocationContext<>() {
                    TestContext testContext;

                    @Override
                    public TestContext getTestContext() {
                        if (testContext == null) {
                            testContext = buildContext(context);
                        }
                        return testContext;
                    }

                    @Override
                    public Object proceed() {
                        return null;
                    }
                }

        );
    }

    @Override
    public int aroundTryProximity() {
        return -5;
    }

    private TestContext buildContext(final TryLifecycleContext context) {
        return new TestContext(
                JqwikMicronautExtension.STORE.get().getApplicationContext(),
                context.optionalContainerClass().orElse(null),
                context.optionalElement().orElse(null),
                null,
                null
        );
    }
}
