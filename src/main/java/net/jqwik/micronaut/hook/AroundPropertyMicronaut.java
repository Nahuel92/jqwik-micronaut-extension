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
    @Override
    @NonNullApi
    @Nonnull
    public PropertyExecutionResult aroundProperty(final PropertyLifecycleContext context,
                                                  final PropertyExecutor property) throws Throwable {
        final var testContext = JqwikMicronautExtension.EXTENSION_STORE.get().testContext(context);
        beforeEach(context, testContext);
        interceptBeforeEach(testContext);
        final var execute = property.execute();
        interceptAfterEach(testContext);
        afterEach(context, testContext);
        return execute;
    }

    private void beforeEach(final PropertyLifecycleContext context, final TestContext testContext) throws Exception {
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
        JqwikMicronautExtension.EXTENSION_STORE.get().beforeTestMethod(testContext);
    }

    private void interceptBeforeEach(final TestContext testContext) throws Throwable {
        JqwikMicronautExtension.EXTENSION_STORE.get().beforeSetupTest(testContext);
        JqwikMicronautExtension.EXTENSION_STORE.get().interceptBeforeEach(
                JqwikMicronautExtension.EXTENSION_STORE.get().getTestMethodInvocationContext(testContext)
        );
        JqwikMicronautExtension.EXTENSION_STORE.get().afterSetupTest(testContext);
    }

    private void interceptAfterEach(final TestContext testContext) throws Throwable {
        JqwikMicronautExtension.EXTENSION_STORE.get().beforeCleanupTest(testContext);
        JqwikMicronautExtension.EXTENSION_STORE.get().interceptAfterEach(
                JqwikMicronautExtension.EXTENSION_STORE.get().getTestMethodInvocationContext(testContext)
        );
        JqwikMicronautExtension.EXTENSION_STORE.get().afterCleanupTest(testContext);
    }

    private void afterEach(final PropertyLifecycleContext context,
                           final TestContext testContext) throws Throwable {
        JqwikMicronautExtension.EXTENSION_STORE.get().afterEach(context);
        JqwikMicronautExtension.EXTENSION_STORE.get().afterTestMethod(testContext);
    }


    @Override
    public int aroundPropertyProximity() {
        /* Property lifecycle methods (@BeforeProperty, @AfterProperty) use -10.
           Smaller numbers means "further away" from actual invocation of property method.
           -20 is therefore around the lifecycle methods. */
        return -20;
    }
}
