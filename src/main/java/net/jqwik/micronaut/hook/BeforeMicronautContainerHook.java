package net.jqwik.micronaut.hook;

import io.micronaut.test.annotation.MicronautTestValue;
import net.jqwik.api.lifecycle.BeforeContainerHook;
import net.jqwik.api.lifecycle.ContainerLifecycleContext;
import net.jqwik.micronaut.annotation.JqwikMicronautTest;
import net.jqwik.micronaut.extension.JqwikMicronautExtension;
import org.junit.platform.commons.support.AnnotationSupport;

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
        return AnnotationSupport
                .findAnnotation(testClass, JqwikMicronautTest.class)
                .map(this::buildValueObject)
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
}
