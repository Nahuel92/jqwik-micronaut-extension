package net.jqwik.micronaut.hook.test.lifecycle;

import jakarta.annotation.Nonnull;
import net.jqwik.api.NonNullApi;
import net.jqwik.api.lifecycle.AroundPropertyHook;
import net.jqwik.api.lifecycle.PropertyExecutionResult;
import net.jqwik.api.lifecycle.PropertyExecutor;
import net.jqwik.api.lifecycle.PropertyLifecycleContext;
import net.jqwik.micronaut.extension.JqwikMicronautExtension;

public class AroundTestExecution implements AroundPropertyHook {
    private final JqwikMicronautExtension extension;

    AroundTestExecution() {
        this.extension = JqwikMicronautExtension.STORE.get();
    }

    @Override
    @NonNullApi
    @Nonnull
    public PropertyExecutionResult aroundProperty(final PropertyLifecycleContext context,
                                                  final PropertyExecutor property) throws Throwable {
        final var testContext = extension.testContext(context);
        System.out.println("4. beforeTestExecution");
        extension.beforeTestExecution(testContext);
        final var execute = property.execute();
        System.out.println("6. afterTestExecution");
        extension.afterTestExecution(testContext);
        return execute;
    }

    @Override
    public int aroundPropertyProximity() {
        return -15;
    }
}
