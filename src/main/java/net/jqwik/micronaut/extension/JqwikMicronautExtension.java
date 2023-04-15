package net.jqwik.micronaut.extension;

import io.micronaut.context.ApplicationContext;
import io.micronaut.context.annotation.Property;
import io.micronaut.test.annotation.MicronautTestValue;
import io.micronaut.test.annotation.MockBean;
import io.micronaut.test.context.*;
import io.micronaut.test.extensions.AbstractMicronautExtension;
import io.micronaut.test.support.TestPropertyProvider;

import net.jqwik.api.lifecycle.*;
import net.jqwik.engine.support.*;
import net.jqwik.micronaut.annotation.*;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Map;

public class JqwikMicronautExtension extends AbstractMicronautExtension<LifecycleContext> {
    public static final Store<JqwikMicronautExtension> STORE = Store.getOrCreate(
            JqwikMicronautExtension.class,
            Lifespan.RUN,
            JqwikMicronautExtension::new
    );

    public ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    public void beforeContainer(final ContainerLifecycleContext context) throws Exception {
        System.out.println("1. beforeContainer");
        final MicronautTestValue micronautTestValue = buildMicronautTestValue(context.optionalContainerClass().orElse(null));
        beforeClass(context, context.optionalContainerClass().orElse(null), micronautTestValue);
        beforeTestClass(buildContainerContext(context));
    }

    public void afterContainer(final ContainerLifecycleContext context) throws Exception {
        System.out.println("9. afterContainer");
        afterTestClass(buildContainerContext(context));
        afterClass(context);
    }

    public void beforeProperty(final PropertyLifecycleContext context) throws Exception {
        System.out.println("2. beforeProperty");
        final var testContext = buildPropertyContext(context);
        injectEnclosingTestInstances(context);
        beforeEach(
            context,
            context.testInstance(),
            context.targetMethod(),
            JqwikAnnotationSupport.findRepeatableAnnotationOnElementOrContainer(
                context.optionalElement().orElse(null),
                Property.class
            )
        );
        beforeTestMethod(testContext);
    }

    public void afterProperty(final PropertyLifecycleContext context) throws Throwable {
        System.out.println("8. afterProperty");
        final var testContext = buildPropertyContext(context);
        afterEach(context);
        afterTestMethod(testContext);
    }


    public void preBeforePropertyMethod(PropertyLifecycleContext context) throws Throwable {
        System.out.println("3.1. preBeforePropertyMethod");
        final var testContext = buildPropertyContext(context);
        beforeSetupTest(testContext);
    }

    public void postBeforePropertyMethod(PropertyLifecycleContext context) throws Throwable {
        System.out.println("3.2. postBeforePropertyMethod");
        final var testContext = buildPropertyContext(context);
        afterSetupTest(testContext);
    }

    public void preAfterPropertyMethod(PropertyLifecycleContext context) throws Throwable {
        System.out.println("7.1. preAfterPropertyMethod");
        final var testContext = buildPropertyContext(context);
        beforeCleanupTest(testContext);
    }

    public void postAfterPropertyMethod(PropertyLifecycleContext context) throws Throwable {
        System.out.println("7.2. postAfterPropertyMethod");
        final var testContext = buildPropertyContext(context);
        afterCleanupTest(testContext);
    }

    public void beforePropertyExecution(PropertyLifecycleContext context) throws Exception {
        System.out.println("4. beforePropertyExecution");
        final var testContext = buildPropertyContext(context);
        beforeTestExecution(testContext);
    }

    public void afterPropertyExecution(PropertyLifecycleContext context) throws Exception {
        System.out.println("6. afterPropertyExecution");
        final var testContext = buildPropertyContext(context);
        afterTestExecution(testContext);
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

    private void injectEnclosingTestInstances(final LifecycleContext lifecycleContext) {
        if (lifecycleContext instanceof PropertyLifecycleContext plc) {
            plc.testInstances().forEach(applicationContext::inject);
        }
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
                                     .orElseGet(() ->
                                                    JqwikAnnotationSupport.findContainerAnnotations(testClass.getSuperclass(), JqwikMicronautTest.class)
                                                                          .stream()
                                                                          .map(this::buildValueObject)
                                                                          .findFirst()
                                                                          .orElse(null)
                                     );
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

    private TestContext buildPropertyContext(final PropertyLifecycleContext context) {
        return new TestContext(
            applicationContext,
            context.containerClass(),
            context.targetMethod(),
            context.testInstance(),
            null // TODO: How to handle exceptions that occur during hook executions?
        );
    }

    private TestContext buildContainerContext(final ContainerLifecycleContext context) {
        return new TestContext(
            JqwikMicronautExtension.STORE.get().getApplicationContext(),
            context.optionalContainerClass().orElse(null),
            context.optionalElement().orElse(null),
            null,
            null
        );
    }

}
