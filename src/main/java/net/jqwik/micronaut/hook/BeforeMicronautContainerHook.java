package net.jqwik.micronaut.hook;

import io.micronaut.test.annotation.MicronautTestValue;
import io.micronaut.test.support.TestPropertyProvider;
import net.jqwik.api.lifecycle.BeforeContainerHook;
import net.jqwik.api.lifecycle.ContainerLifecycleContext;
import net.jqwik.api.lifecycle.LifecycleContext;
import net.jqwik.engine.support.JqwikAnnotationSupport;
import net.jqwik.micronaut.annotation.JqwikMicronautTest;
import net.jqwik.micronaut.extension.JqwikMicronautExtension;

import java.util.Map;

public class BeforeMicronautContainerHook extends JqwikMicronautExtension implements BeforeContainerHook {
    @Override
    public void beforeContainer(final ContainerLifecycleContext context) {
        EXTENSION_STORE.get()
                .beforeClass(
                        context,
                        context.optionalContainerClass().orElse(null),
                        buildMicronautTestValue(context.optionalContainerClass().orElse(null))
                );
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
                .orElse(null);
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

    @Override
    protected void resolveTestProperties(LifecycleContext context, MicronautTestValue testAnnotationValue, Map<String, Object> testProperties) {
        if (context.optionalContainerClass().isEmpty()) {
            return;
        }
        final Class<?> testContainerClass = context.optionalContainerClass().get();
        final Object testClassInstance = context.newInstance(testContainerClass);

        if (testClassInstance instanceof TestPropertyProvider propertyProvider) {
            final Map<String, String> dynamicPropertiesToAdd = propertyProvider.getProperties();
            testProperties.putAll(dynamicPropertiesToAdd);
        }
    }
}
