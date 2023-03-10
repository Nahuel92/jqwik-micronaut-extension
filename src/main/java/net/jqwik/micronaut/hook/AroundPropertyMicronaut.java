package net.jqwik.micronaut.hook;

import io.micronaut.test.annotation.MicronautTestValue;
import net.jqwik.api.lifecycle.AroundPropertyHook;
import net.jqwik.api.lifecycle.PropertyExecutionResult;
import net.jqwik.api.lifecycle.PropertyExecutor;
import net.jqwik.api.lifecycle.PropertyLifecycleContext;
import net.jqwik.micronaut.annotation.JqwikMicronautTest;
import net.jqwik.micronaut.extension.JqwikMicronautExtension;
import org.junit.platform.commons.support.AnnotationSupport;

import java.util.List;

public class AroundPropertyMicronaut extends JqwikMicronautExtension implements AroundPropertyHook {
    @Override
    public PropertyExecutionResult aroundProperty(final PropertyLifecycleContext context,
                                                  final PropertyExecutor property) {
        // Without this `beforeClass` here, the test fails. It seems that the initialization done
        // in the `BeforeContextHook` is lost at this point.
        beforeClass(
                context,
                context.optionalContainerClass().orElse(null),
                buildMicronautTestValue(context.optionalContainerClass().orElse(null))
        );
        beforeEach(
                context,
                context.testInstance(),
                context.targetMethod(),
                List.of()
        );
        return property.execute();
    }

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
                true
        );
    }
}
