package net.jqwik.micronaut.hook.test.lifecycle;

import jakarta.annotation.Nonnull;
import net.jqwik.api.NonNullApi;
import net.jqwik.api.lifecycle.AroundPropertyHook;
import net.jqwik.api.lifecycle.PropertyExecutionResult;
import net.jqwik.api.lifecycle.PropertyExecutor;
import net.jqwik.api.lifecycle.PropertyLifecycleContext;
import net.jqwik.micronaut.extension.JqwikMicronautExtension;

// TODO: Maybe this should be an AroundTryHook?
public class AroundPropertyExecution implements AroundPropertyHook {
    private final JqwikMicronautExtension extension;

    AroundPropertyExecution() {
        this.extension = JqwikMicronautExtension.STORE.get();
    }

    @Override
    @NonNullApi
    @Nonnull
    public PropertyExecutionResult aroundProperty(final PropertyLifecycleContext context,
                                                  final PropertyExecutor property) throws Throwable {
        extension.beforePropertyExecution(context);
        // TODO: use property.executeAndFinally(..)
        final var execute = property.execute();
        extension.afterPropertyExecution(context);
        return execute;
    }

    @Override
    public int aroundPropertyProximity() {
        // In-between @BeforeProperty, @AfterProperty and actual property execution
        return -5;
    }
}
