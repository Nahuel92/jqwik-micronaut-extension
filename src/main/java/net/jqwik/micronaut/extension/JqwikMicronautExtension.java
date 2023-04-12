package net.jqwik.micronaut.extension;

import io.micronaut.context.ApplicationContext;
import io.micronaut.context.annotation.Property;
import io.micronaut.test.annotation.MicronautTestValue;
import io.micronaut.test.annotation.MockBean;
import io.micronaut.test.context.TestContext;
import io.micronaut.test.context.TestMethodInvocationContext;
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
    public static final Store<JqwikMicronautExtension> STORE = Store.getOrCreate(
            JqwikMicronautExtension.class,
            Lifespan.RUN,
            JqwikMicronautExtension::new
    );
    private static TestContext testContext;

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
        injectEnclosingTestInstances(context);
        super.beforeEach(context, testInstance, method, propertyAnnotations);
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

                    final var mockBeanFields = Arrays.stream(specInstance.getClass().getDeclaredFields())
                            .filter(e -> e.isAnnotationPresent(MockBean.class))
                            .toList();

                    for (final var injectedField : specDefinition.getInjectedFields()) {
                        final var mockBeanMethod = mockBeanMethods.stream()
                                .filter(e -> e.getReturnType().equals(injectedField.getType()))
                                .findFirst();

                        final var mockBeanField = mockBeanFields.stream()
                                .filter(e -> e.getType().equals(injectedField.getType()))
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

                        mockBeanField.ifPresent(e -> {
                            try {
                                final var field = injectedField.getField();
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

    public TestContext testContext(final PropertyLifecycleContext context) {
        if (testContext != null) {
            return testContext;
        }
        testContext = new TestContext(
                applicationContext,
                context.containerClass(),
                context.targetMethod(),
                context.testInstance(),
                null
        );
        return testContext;
    }

    public TestMethodInvocationContext<Object> getTestMethodInvocationContext(final TestContext testContext) {
        return new TestMethodInvocationContext<>() {
            @Override
            public TestContext getTestContext() {
                return testContext;
            }

            @Override
            public Object proceed() {
                return null;
            }
        };
    }

    public void injectEnclosingTestInstances(final LifecycleContext lifecycleContext) {
        if (lifecycleContext instanceof PropertyLifecycleContext plc) {
            plc.testInstances().forEach(applicationContext::inject);
        }
    }
}
