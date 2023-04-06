package net.jqwik.micronaut.hook;

import io.micronaut.test.annotation.MicronautTestValue;
import io.micronaut.test.context.TestContext;
import net.jqwik.api.NonNullApi;
import net.jqwik.api.lifecycle.BeforeContainerHook;
import net.jqwik.api.lifecycle.ContainerLifecycleContext;
import net.jqwik.engine.support.JqwikAnnotationSupport;
import net.jqwik.micronaut.annotation.JqwikMicronautTest;
import net.jqwik.micronaut.extension.JqwikMicronautExtension;

public class BeforeMicronautContainerHook implements BeforeContainerHook {
    @Override
    @NonNullApi
    public void beforeContainer(final ContainerLifecycleContext context) throws Exception {
        JqwikMicronautExtension.EXTENSION_STORE.get()
                .beforeClass(
                        context,
                        context.optionalContainerClass().orElse(null),
                        buildMicronautTestValue(context.optionalContainerClass().orElse(null))
                );
        final TestContext testContext = buildContext(context);
        JqwikMicronautExtension.EXTENSION_STORE.get().beforeTestClass(testContext);
    }

    /**
     * Builds a {@link MicronautTestValue} object from the provided class (e.g. by scanning annotations).
     *
     * @param testClass the class to extract builder configuration from
     * @return a MicronautTestValue to configure the test application context
     */
    private MicronautTestValue buildMicronautTestValue(final Class<?> testClass) {
        return JqwikAnnotationSupport.findContainerAnnotations(testClass, JqwikMicronautTest.class)
                .stream()
                .map(this::buildValueObject)
                .findFirst()
                .orElseGet(() ->
                        JqwikAnnotationSupport.findContainerAnnotations(testClass.getSuperclass(), JqwikMicronautTest.class)
                                .stream()
                                .map(this::buildValueObject)
                                .findFirst()
                                .orElse(null)
                );
    }

    private MicronautTestValue buildValueObject(final JqwikMicronautTest micronautTest) {
        return new MicronautTestValue(
                micronautTest.application(),
                micronautTest.environments(),
                micronautTest.packages(),
                micronautTest.propertySources(),
                micronautTest.rollback(),
                micronautTest.transactional(),
                micronautTest.rebuildContext(),
                micronautTest.contextBuilder(),
                micronautTest.transactionMode(),
                micronautTest.startApplication(),
                micronautTest.resolveParameters()
        );
    }

    private TestContext buildContext(final ContainerLifecycleContext context) {
        return new TestContext(
                JqwikMicronautExtension.EXTENSION_STORE.get().getApplicationContext(),
                context.optionalContainerClass().orElse(null),
                context.optionalElement().orElse(null),
                null,
                null
        );
    }
}
