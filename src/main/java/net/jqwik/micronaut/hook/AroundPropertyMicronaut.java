package net.jqwik.micronaut.hook;

import io.micronaut.context.annotation.Property;
import io.micronaut.test.annotation.MicronautTestValue;
import net.jqwik.api.lifecycle.*;
import net.jqwik.engine.support.JqwikAnnotationSupport;
import net.jqwik.micronaut.extension.JqwikMicronautExtension;

import java.util.Map;

public class AroundPropertyMicronaut extends JqwikMicronautExtension implements AroundPropertyHook {
    @Override
    public PropertyExecutionResult aroundProperty(final PropertyLifecycleContext context,
                                                  final PropertyExecutor property) {
        EXTENSION_STORE.get()
                .beforeEach(
                        context,
                        context.testInstance(),
                        context.targetMethod(),
                        JqwikAnnotationSupport.findRepeatableAnnotationOnElementOrContainer(
                                context.optionalElement().orElse(null),
                                Property.class
                        )
                );
        return property.execute();
    }

    @Override
    public void resolveTestProperties(LifecycleContext context, MicronautTestValue testAnnotationValue, Map<String, Object> testProperties) {

    }
}
