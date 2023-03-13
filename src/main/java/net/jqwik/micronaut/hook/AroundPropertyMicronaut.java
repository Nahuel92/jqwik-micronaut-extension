package net.jqwik.micronaut.hook;

import io.micronaut.aop.InterceptedProxy;
import io.micronaut.context.ApplicationContext;
import io.micronaut.core.util.CollectionUtils;
import io.micronaut.inject.FieldInjectionPoint;
import io.micronaut.test.annotation.MicronautTestValue;
import io.micronaut.test.annotation.MockBean;
import io.micronaut.test.context.TestContext;
import io.micronaut.test.extensions.AbstractMicronautExtension;
import io.micronaut.test.support.TestPropertyProvider;
import net.jqwik.api.Tuple;
import net.jqwik.api.lifecycle.*;
import net.jqwik.micronaut.annotation.JqwikMicronautTest;
import org.junit.platform.commons.support.AnnotationSupport;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Optional;

public class AroundPropertyMicronaut extends AbstractMicronautExtension<LifecycleContext> implements AroundPropertyHook {
    @Override
    public PropertyExecutionResult aroundProperty(final PropertyLifecycleContext context,
                                                  final PropertyExecutor property) {
        beforeClass(
                context,
                context.optionalContainerClass().orElse(null),
                buildMicronautTestValue(context.optionalContainerClass().orElse(null))
        );
        return property.execute();
    }

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

    private Store<TestContext> getStore(final LifecycleContext context) {
        return Store.getOrCreate(storeIdentifier(context.optionalContainerClass().orElse(null)),
                Lifespan.RUN,
                () -> new TestContext(
                        applicationContext,
                        context.optionalContainerClass().orElse(null),
                        getTestMethod(context).orElse(null),
                        getTestInstance(context),
                        null
                ));
    }

    private Tuple.Tuple2<?, ?> storeIdentifier(final Class<?> containerClass) {
        return Tuple.of(JqwikMicronautTest.class, containerClass);
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
                true
        );
    }

    private Optional<Object> getTestInstance(final LifecycleContext context) {
        if (context instanceof PropertyLifecycleContext c) {
            return Optional.of(c.testInstance());
        }
        if (context instanceof TryLifecycleContext c) {
            return Optional.of(c.testInstance());
        }
        return Optional.empty();
    }

    private Optional<Method> getTestMethod(final LifecycleContext context) {
        if (context instanceof PropertyLifecycleContext c) {
            return Optional.of(c.targetMethod());
        }
        if (context instanceof TryLifecycleContext c) {
            return Optional.of(c.targetMethod());
        }
        return Optional.empty();
    }
}
