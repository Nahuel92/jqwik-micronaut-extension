package net.jqwik.micronaut.hook;

import io.micronaut.context.annotation.Property;
import jakarta.annotation.Nonnull;
import net.jqwik.api.NonNullApi;
import net.jqwik.api.lifecycle.AroundPropertyHook;
import net.jqwik.api.lifecycle.PropertyExecutionResult;
import net.jqwik.api.lifecycle.PropertyExecutor;
import net.jqwik.api.lifecycle.PropertyLifecycleContext;
import net.jqwik.engine.support.JqwikAnnotationSupport;
import net.jqwik.micronaut.extension.JqwikMicronautExtension;

public class AroundPropertyMicronaut implements AroundPropertyHook {
    @Override
    @NonNullApi
    @Nonnull
    public PropertyExecutionResult aroundProperty(final PropertyLifecycleContext context,
                                                  final PropertyExecutor property) {
        JqwikMicronautExtension.EXTENSION_STORE.get()
                .beforeEach(
                        context,
                        context.testInstance(),
                        context.targetMethod(),
                        JqwikAnnotationSupport.findRepeatableAnnotationOnElementOrContainer(
                                context.optionalElement().orElse(null),
                                Property.class
                        )
                );
        return property.execute();
    }
}
