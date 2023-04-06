package net.jqwik.micronaut.hook;

import jakarta.annotation.Nonnull;
import net.jqwik.api.NonNullApi;
import net.jqwik.api.lifecycle.AroundPropertyHook;
import net.jqwik.api.lifecycle.PropertyExecutionResult;
import net.jqwik.api.lifecycle.PropertyExecutor;
import net.jqwik.api.lifecycle.PropertyLifecycleContext;
import net.jqwik.micronaut.extension.JqwikMicronautExtension;

public class AfterPropertyHook implements AroundPropertyHook {
    @Override
    @NonNullApi
    @Nonnull
    public PropertyExecutionResult aroundProperty(final PropertyLifecycleContext context,
                                                  final PropertyExecutor property) throws Throwable {
        final var t = JqwikMicronautExtension.EXTENSION_STORE.get().testContext(context);
        JqwikMicronautExtension.EXTENSION_STORE.get().beforeCleanupTest(t);
        JqwikMicronautExtension.EXTENSION_STORE.get().afterCleanupTest(t);
        return property.execute();
    }

    @Override
    public int aroundPropertyProximity() {
        return -14;
    }
}