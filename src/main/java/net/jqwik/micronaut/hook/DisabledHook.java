package net.jqwik.micronaut.hook;

import io.micronaut.context.ApplicationContext;
import io.micronaut.context.annotation.Requires;
import jakarta.annotation.Nonnull;
import net.jqwik.api.NonNullApi;
import net.jqwik.api.lifecycle.LifecycleContext;
import net.jqwik.api.lifecycle.SkipExecutionHook;
import net.jqwik.micronaut.extension.JqwikMicronautExtension;

public class DisabledHook implements SkipExecutionHook {
    @Override
    @NonNullApi
    @Nonnull
    public SkipResult shouldBeSkipped(final LifecycleContext context) {
        final ApplicationContext applicationContext = JqwikMicronautExtension.EXTENSION_STORE.get().getApplicationContext();
        final boolean isAnyPropertyMissing = context.findAnnotationsInContainer(Requires.class)
                .stream()
                .map(Requires::property)
                .anyMatch(e -> !applicationContext.containsProperties(e));

        if (isAnyPropertyMissing) {
            return SkipResult.skip("@Requires contains a property that doesn't exists!");
        }
        return SkipResult.doNotSkip();
    }
}