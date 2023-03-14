package net.jqwik.micronaut.extension;

import io.micronaut.aop.InterceptedProxy;
import io.micronaut.context.ApplicationContext;
import io.micronaut.core.util.CollectionUtils;
import io.micronaut.inject.FieldInjectionPoint;
import io.micronaut.test.annotation.MicronautTestValue;
import io.micronaut.test.annotation.MockBean;
import io.micronaut.test.extensions.AbstractMicronautExtension;
import io.micronaut.test.support.TestPropertyProvider;
import net.jqwik.api.lifecycle.LifecycleContext;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Optional;

public class JqwikMicronautExtension extends AbstractMicronautExtension<LifecycleContext> {
    @Override
    protected void resolveTestProperties(final LifecycleContext context, final MicronautTestValue testAnnotationValue,
                                         final Map<String, Object> testProperties) {
        Object o = context.optionalContainerClass().orElse(null);
        if (o instanceof TestPropertyProvider) {
            Map<String, String> properties = ((TestPropertyProvider) o).getProperties();
            if (CollectionUtils.isNotEmpty(properties)) {
                testProperties.putAll(properties);
            }
        }
    }

    @Override
    protected void alignMocks(final LifecycleContext context, final Object instance) {
        if (specDefinition == null) {
            return;
        }
        findSpecInstance(applicationContext).ifPresent(specInstance -> {
            for (FieldInjectionPoint injectedField : specDefinition.getInjectedFields()) {
                final boolean isMock = applicationContext.resolveMetadata(injectedField.getType())
                        .isAnnotationPresent(MockBean.class);
                if (isMock) {
                    final Field field = injectedField.getField();
                    field.setAccessible(true);
                    try {
                        final Object mock = field.get(specInstance);
                        if (mock instanceof InterceptedProxy ip) {
                            final Object target = ip.interceptedTarget();
                            field.set(specInstance, target);
                        }
                    } catch (IllegalAccessException e) {
                        // continue
                    }
                }
            }
        });
    }

    private Optional<?> findSpecInstance(final ApplicationContext context) {
        return context.findBean(specDefinition.getBeanType());
    }
}
