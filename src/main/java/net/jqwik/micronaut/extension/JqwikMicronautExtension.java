package net.jqwik.micronaut.extension;

import io.micronaut.context.ApplicationContext;
import io.micronaut.context.annotation.Property;
import io.micronaut.test.annotation.MicronautTestValue;
import io.micronaut.test.annotation.MockBean;
import io.micronaut.test.context.TestContext;
import io.micronaut.test.extensions.AbstractMicronautExtension;
import io.micronaut.test.support.TestPropertyProvider;
import net.jqwik.api.lifecycle.ContainerLifecycleContext;
import net.jqwik.api.lifecycle.LifecycleContext;
import net.jqwik.api.lifecycle.Lifespan;
import net.jqwik.api.lifecycle.PropertyLifecycleContext;
import net.jqwik.api.lifecycle.Store;
import net.jqwik.engine.support.JqwikAnnotationSupport;
import net.jqwik.micronaut.annotation.JqwikMicronautTest;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

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
        final var micronautTestValue = buildMicronautTestValue(context.optionalContainerClass().orElse(null));
        beforeClass(context, context.optionalContainerClass().orElse(null), micronautTestValue);
        beforeTestClass(buildContainerContext(context));
    }

    public void afterContainer(final ContainerLifecycleContext context) throws Exception {
        afterTestClass(buildContainerContext(context));
        afterClass(context);
    }

    public void beforeProperty(final PropertyLifecycleContext context) throws Exception {
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

    public void afterProperty(final PropertyLifecycleContext context) {
        final var testContext = buildPropertyContext(context);
        runHooks(() -> {
            afterEach(context);
            afterTestMethod(testContext);
            return null;
        });
    }

    public void preBeforePropertyMethod(final PropertyLifecycleContext context) throws Throwable {
        final var testContext = buildPropertyContext(context);
        beforeSetupTest(testContext);
    }

    public void postBeforePropertyMethod(final PropertyLifecycleContext context) throws Throwable {
        final var testContext = buildPropertyContext(context);
        afterSetupTest(testContext);
    }

    public void preAfterPropertyMethod(final PropertyLifecycleContext context) {
        final var testContext = buildPropertyContext(context);
        runHooks(() -> {
            beforeCleanupTest(testContext);
            return null;
        });
    }

    public void postAfterPropertyMethod(final PropertyLifecycleContext context) {
        final var testContext = buildPropertyContext(context);
        runHooks(() -> {
            afterCleanupTest(testContext);
            return null;
        });
    }

    public void beforePropertyExecution(final PropertyLifecycleContext context) throws Exception {
        final var testContext = buildPropertyContext(context);
        beforeTestExecution(testContext);
    }

    public void afterPropertyExecution(final PropertyLifecycleContext context) {
        final var testContext = buildPropertyContext(context);
        runHooks(() -> {
            afterTestExecution(testContext);
            return null;
        });
    }

    @Override
    protected void resolveTestProperties(final LifecycleContext context, final MicronautTestValue testAnnotationValue,
                                         final Map<String, Object> testProperties) {
        context.optionalContainerClass()
                .map(context::newInstance)
                .filter(TestPropertyProvider.class::isInstance)
                .map(TestPropertyProvider.class::cast)
                .map(TestPropertyProvider::getProperties)
                .ifPresent(testProperties::putAll);
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
                .ifPresent(injectMocks());
    }

    private Consumer<Object> injectMocks() {
        return specInstance -> {
            final Class<?> specInstanceClass = specInstance.getClass();
            final var mockBeanAnnotatedMethods = getMockBeanAnnotated(specInstanceClass.getDeclaredMethods());
            final var mockBeanAnnotatedFields = getMockBeanAnnotated(specInstanceClass.getDeclaredFields());
            for (final var injectedField : specDefinition.getInjectedFields()) {
                mockBeanAnnotatedMethods.stream()
                        .filter(e -> e.getReturnType().equals(injectedField.getType()))
                        .findFirst()
                        .ifPresent(e -> {
                                    final Callable<?> mock = () -> e.invoke(specInstance);
                                    injectMock(specInstance, injectedField.getField(), e, mock);
                                }
                        );
                mockBeanAnnotatedFields.stream()
                        .filter(e -> e.getType().equals(injectedField.getType()))
                        .findFirst()
                        .ifPresent(e -> {
                            final Callable<?> mock = () -> e.get(specInstance);
                            injectMock(specInstance, injectedField.getField(), e, mock);
                        });
            }
        };
    }

    @SafeVarargs
    private <T extends AccessibleObject> List<T> getMockBeanAnnotated(final T... accessibleObject) {
        return Arrays.stream(accessibleObject)
                .filter(e -> e.isAnnotationPresent(MockBean.class))
                .toList();
    }

    private void injectMock(final Object specInstance, final Field fieldToInject,
                            final AccessibleObject accessibleObject, final Callable<?> mockProvider) {
        fieldToInject.setAccessible(true);
        accessibleObject.setAccessible(true);
        try {
            fieldToInject.set(specInstance, mockProvider.call());
        } catch (final Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private void runHooks(final Callable<Void> hooks) {
        try {
            hooks.call();
        } catch (final Exception e) {
            throw new RuntimeException(e);
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
                applicationContext,
                context.optionalContainerClass().orElse(null),
                context.optionalElement().orElse(null),
                null,
                null
        );
    }
}
