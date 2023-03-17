package net.jqwik.micronaut.extension;

import io.micronaut.context.annotation.Property;
import io.micronaut.core.util.CollectionUtils;
import io.micronaut.test.annotation.MicronautTestValue;
import io.micronaut.test.annotation.MockBean;
import io.micronaut.test.extensions.AbstractMicronautExtension;
import io.micronaut.test.support.TestPropertyProvider;
import net.jqwik.api.lifecycle.LifecycleContext;
import net.jqwik.api.lifecycle.Lifespan;
import net.jqwik.api.lifecycle.PropertyLifecycleContext;
import net.jqwik.api.lifecycle.Store;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class JqwikMicronautExtension extends AbstractMicronautExtension<LifecycleContext> {
    protected static final Store<JqwikMicronautExtension> EXTENSION_STORE = Store.getOrCreate(
            JqwikMicronautExtension.class,
            Lifespan.RUN,
            JqwikMicronautExtension::new
    );

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
    public void resolveTestProperties(final LifecycleContext context, final MicronautTestValue testAnnotationValue,
                                      final Map<String, Object> testProperties) {
        if (context instanceof PropertyLifecycleContext plc &&
                plc.testInstance() instanceof TestPropertyProvider tpp) {
            final Map<String, String> properties = tpp.getProperties();
            if (CollectionUtils.isNotEmpty(properties)) {
                testProperties.putAll(properties);
            }
        }
    }

    @Override
    protected void alignMocks(final LifecycleContext context, final Object instance) {
        if (specDefinition == null || !(context instanceof PropertyLifecycleContext plc)) {
            return;
        }
        plc.testInstances()
                .stream()
                .filter(e -> e.getClass().equals(specDefinition.getBeanType()))
                .findAny()
                .ifPresent(specInstance -> {
                    final var mockBeanMethods = Arrays.stream(specInstance.getClass().getDeclaredMethods())
                            .filter(e -> e.isAnnotationPresent(MockBean.class))
                            .toList();

                    for (final var injectedField : specDefinition.getInjectedFields()) {
                        final var mockBeanMethod = mockBeanMethods.stream()
                                .filter(e -> e.getReturnType().equals(injectedField.getType()))
                                .findFirst();

                        mockBeanMethod.ifPresent(e -> {
                                    try {
                                        final var field = injectedField.getField();
                                        field.setAccessible(true);
                                        e.setAccessible(true);
                                        final Object result = e.invoke(specInstance);
                                        field.set(specInstance, result);
                                    } catch (final IllegalAccessException | InvocationTargetException ex) {
                                        throw new RuntimeException(ex);
                                    }
                                }
                        );
                    }
                });
    }
}
