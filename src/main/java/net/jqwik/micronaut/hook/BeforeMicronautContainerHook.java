package net.jqwik.micronaut.hook;

import io.micronaut.test.annotation.MicronautTestValue;
import net.jqwik.api.lifecycle.BeforeContainerHook;
import net.jqwik.api.lifecycle.ContainerLifecycleContext;
import net.jqwik.api.lifecycle.LifecycleContext;
import net.jqwik.engine.support.JqwikAnnotationSupport;
import net.jqwik.micronaut.annotation.JqwikMicronautTest;
import net.jqwik.micronaut.extension.JqwikMicronautExtension;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
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
        final var propertiesMethodOptional = Arrays.stream(testContainerClass.getDeclaredMethods())
                .filter(e -> "getProperties".equals(e.getName()))   // Brittle, I don't like it.
                .findFirst();

        if (propertiesMethodOptional.isEmpty()) {
            return;
        }
        final var propertiesMethod = propertiesMethodOptional.get();
        propertiesMethod.setAccessible(true);

        final Map<String, String> dynamicPropertiesToAdd;
        final Object testClassInstance = context.newInstance(testContainerClass);
        try {
            dynamicPropertiesToAdd = getPropertiesFromTestClass(propertiesMethod, testClassInstance);
        } catch (final IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
        testProperties.putAll(dynamicPropertiesToAdd);
    }

    @SuppressWarnings("unchecked")
    private Map<String, String> getPropertiesFromTestClass(final Method propertiesMethod, final Object testClassInstance)
            throws InvocationTargetException, IllegalAccessException {
        return (Map<String, String>) propertiesMethod.invoke(testClassInstance);
    }
}
