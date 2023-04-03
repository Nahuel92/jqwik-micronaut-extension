package net.jqwik.micronaut.extension;

import io.micronaut.context.ApplicationContext;
import io.micronaut.context.annotation.Property;
import io.micronaut.inject.FieldInjectionPoint;
import io.micronaut.test.annotation.MicronautTestValue;
import io.micronaut.test.annotation.MockBean;
import io.micronaut.test.extensions.AbstractMicronautExtension;
import io.micronaut.test.support.TestPropertyProvider;
import net.jqwik.api.lifecycle.LifecycleContext;
import net.jqwik.api.lifecycle.Lifespan;
import net.jqwik.api.lifecycle.PropertyLifecycleContext;
import net.jqwik.api.lifecycle.Store;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class JqwikMicronautExtension extends AbstractMicronautExtension<LifecycleContext> {
    public static final Store<JqwikMicronautExtension> EXTENSION_STORE = Store.getOrCreate(
            JqwikMicronautExtension.class,
            Lifespan.RUN,
            JqwikMicronautExtension::new
    );

    public ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    @Override
    public void beforeClass(final LifecycleContext context, final Class<?> testClass,
                            final MicronautTestValue testAnnotationValue) {
        super.beforeClass(context, testClass, testAnnotationValue);
    }

    @Override
    public void beforeEach(final LifecycleContext context, final Object testInstance,
                           final AnnotatedElement method, final List<Property> propertyAnnotations) {
        super.beforeEach(context, testInstance, method, propertyAnnotations);
    }

    @Override
    protected void alignMocks(final LifecycleContext context, final Object instance) {
        if (specDefinition == null || !(context instanceof PropertyLifecycleContext)) {
            return;
        }
        final PropertyLifecycleContext plc = (PropertyLifecycleContext) context;
        plc.testInstances()
                .stream()
                .filter(e -> e.getClass().equals(specDefinition.getBeanType()))
                .findAny()
                .ifPresent(specInstance -> {
                    final List<Method> mockBeanMethods = Arrays.stream(specInstance.getClass().getDeclaredMethods())
                            .filter(e -> e.isAnnotationPresent(MockBean.class))
                            .collect(Collectors.toList());

                    final List<Field> mockBeanFields = Arrays.stream(specInstance.getClass().getDeclaredFields())
                            .filter(e -> e.isAnnotationPresent(MockBean.class))
                            .collect(Collectors.toList());

                    for (final FieldInjectionPoint<?, ?> injectedField : specDefinition.getInjectedFields()) {
                        final Optional<Method> mockBeanMethod = mockBeanMethods.stream()
                                .filter(e -> e.getReturnType().equals(injectedField.getType()))
                                .findFirst();

                        final Optional<Field> mockBeanField = mockBeanFields.stream()
                                .filter(e -> e.getType().equals(injectedField.getType()))
                                .findFirst();

                        mockBeanMethod.ifPresent(e -> {
                            try {
                                final Field field = injectedField.getField();
                                        field.setAccessible(true);
                                        e.setAccessible(true);
                                        final Object result = e.invoke(specInstance);
                                        field.set(specInstance, result);
                                    } catch (final IllegalAccessException | InvocationTargetException ex) {
                                        throw new RuntimeException(ex);
                                    }
                                }
                        );

                        mockBeanField.ifPresent(e -> {
                            try {
                                final Field field = injectedField.getField();
                                field.setAccessible(true);
                                e.setAccessible(true);
                                field.set(specInstance, e.get(specInstance));
                            } catch (final IllegalAccessException ex) {
                                throw new RuntimeException(ex);
                            }
                        });
                    }
                });
    }

    @Override
    protected void resolveTestProperties(final LifecycleContext context, final MicronautTestValue testAnnotationValue,
                                         final Map<String, Object> testProperties) {
        if (!context.optionalContainerClass().isPresent()) {
            return;
        }
        final Class<?> testContainerClass = context.optionalContainerClass().get();
        final Object testClassInstance = context.newInstance(testContainerClass);

        if (testClassInstance instanceof TestPropertyProvider) {
            final Map<String, String> dynamicPropertiesToAdd = ((TestPropertyProvider) testClassInstance).getProperties();
            testProperties.putAll(dynamicPropertiesToAdd);
        }
    }
}
