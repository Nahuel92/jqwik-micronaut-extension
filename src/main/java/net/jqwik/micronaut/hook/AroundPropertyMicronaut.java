package net.jqwik.micronaut.hook;

import io.micronaut.context.annotation.Property;
import io.micronaut.test.context.TestContext;
import io.micronaut.test.context.TestMethodInvocationContext;
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
        final var testContext = JqwikMicronautExtension.EXTENSION_STORE.get().testContext(context);
        JqwikMicronautExtension.EXTENSION_STORE.get().beforeSetupTest(testContext);
        JqwikMicronautExtension.EXTENSION_STORE.get().beforeCleanupTest(testContext);

        final var execute = property.execute();
        final var test = new TestMethodInvocationContext<>() {
            @Override
            public TestContext getTestContext() {
                return testContext;
            }

            @Override
            public Object proceed() {
                return execute;
            }
        };
        JqwikMicronautExtension.EXTENSION_STORE.get().interceptBeforeEach(test);
        JqwikMicronautExtension.EXTENSION_STORE.get().interceptAfterEach(test);
        JqwikMicronautExtension.EXTENSION_STORE.get().afterCleanupTest(testContext);
        return execute;
    }

    @Override
    public int aroundPropertyProximity() {
        /* Property lifecycle methods (@BeforeProperty, @AfterProperty) use -10.
           Smaller numbers means "further away" from actual invocation of property method.
           -20 is therefore around the lifecycle methods. */
        return -20;
    }
}
