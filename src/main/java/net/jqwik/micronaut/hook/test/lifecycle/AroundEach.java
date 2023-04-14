package net.jqwik.micronaut.hook.test.lifecycle;

import io.micronaut.context.annotation.Property;
import io.micronaut.test.context.TestContext;
import jakarta.annotation.Nonnull;
import net.jqwik.api.NonNullApi;
import net.jqwik.api.lifecycle.AroundPropertyHook;
import net.jqwik.api.lifecycle.PropertyExecutionResult;
import net.jqwik.api.lifecycle.PropertyExecutor;
import net.jqwik.api.lifecycle.PropertyLifecycleContext;
import net.jqwik.engine.support.JqwikAnnotationSupport;
import net.jqwik.micronaut.extension.JqwikMicronautExtension;

public class AroundEach implements AroundPropertyHook {
    private final JqwikMicronautExtension extension;

    AroundEach() {
        this.extension = JqwikMicronautExtension.STORE.get();
    }

    @Override
    @NonNullApi
    @Nonnull
    public PropertyExecutionResult aroundProperty(final PropertyLifecycleContext context,
                                                  final PropertyExecutor property) throws Throwable {
        final var testContext = extension.testContext(context);
        beforeEach(context, testContext);
        final var execute = property.execute();
        afterEach(context, testContext);
        return execute;
    }

    private void beforeEach(final PropertyLifecycleContext context, final TestContext testContext) throws Exception {
        System.out.println("2. beforeEach");
        extension.injectEnclosingTestInstances(context);
        extension.beforeEach(
                context,
                context.testInstance(),
                context.targetMethod(),
                JqwikAnnotationSupport.findRepeatableAnnotationOnElementOrContainer(
                        context.optionalElement().orElse(null),
                        Property.class
                )
        );
        extension.beforeTestMethod(testContext);
    }

    private void afterEach(final PropertyLifecycleContext context, final TestContext testContext) throws Throwable {
        System.out.println("8. afterEach");
        extension.afterEach(context);
        extension.afterTestMethod(testContext);
    }

    @Override
    public int aroundPropertyProximity() {
        /* Property lifecycle methods (@BeforeProperty, @AfterProperty) use -10.
           Smaller numbers means "further away" from actual invocation of property method.
           -20 is therefore around the lifecycle methods. */
        return -20;
    }
}
