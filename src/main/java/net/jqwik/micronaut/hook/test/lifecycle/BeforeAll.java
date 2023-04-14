package net.jqwik.micronaut.hook.test.lifecycle;

import io.micronaut.test.annotation.MicronautTestValue;
import io.micronaut.test.context.TestContext;
import net.jqwik.api.NonNullApi;
import net.jqwik.api.lifecycle.BeforeContainerHook;
import net.jqwik.api.lifecycle.ContainerLifecycleContext;
import net.jqwik.engine.support.JqwikAnnotationSupport;
import net.jqwik.micronaut.annotation.JqwikMicronautTest;
import net.jqwik.micronaut.extension.JqwikMicronautExtension;

public class BeforeAll implements BeforeContainerHook {
    private final JqwikMicronautExtension extension;

    BeforeAll() {
        this.extension = JqwikMicronautExtension.STORE.get();
    }

    @Override
    @NonNullApi
    public void beforeContainer(final ContainerLifecycleContext context) throws Exception {
        beforeAll(context);
    }

    @Override
    public int beforeContainerProximity() {
        return -20;
    }

    private void beforeAll(final ContainerLifecycleContext context) throws Exception {
        System.out.println("1. beforeAll");
        extension.beforeClass(
                context,
                context.optionalContainerClass().orElse(null),
                buildMicronautTestValue(context.optionalContainerClass().orElse(null))
        );
        extension.beforeTestClass(buildContext(context));
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
                JqwikMicronautExtension.STORE.get().getApplicationContext(),
                context.optionalContainerClass().orElse(null),
                context.optionalElement().orElse(null),
                null,
                null
        );
    }
}