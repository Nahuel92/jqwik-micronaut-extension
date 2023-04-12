package net.jqwik.micronaut.hook;

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

public class AroundPropertyMicronaut implements AroundPropertyHook {
    private final JqwikMicronautExtension extension;

    AroundPropertyMicronaut() {
        this.extension = JqwikMicronautExtension.STORE.get();
    }

    @Override
    @NonNullApi
    @Nonnull
    public PropertyExecutionResult aroundProperty(final PropertyLifecycleContext context,
                                                  final PropertyExecutor property) throws Throwable {
        final var testContext = extension.testContext(context);
        beforeEach(context, testContext);
        extension.beforeTestExecution(testContext);
        final var execute = property.execute();
        extension.afterTestExecution(testContext);
        afterEach(context, testContext);
        return execute;
    }

    private void beforeEach(final PropertyLifecycleContext context, final TestContext testContext) throws Exception {
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
