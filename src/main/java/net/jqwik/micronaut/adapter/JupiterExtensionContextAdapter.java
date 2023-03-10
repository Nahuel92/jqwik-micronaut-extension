package net.jqwik.micronaut.adapter;

import net.jqwik.api.lifecycle.LifecycleContext;
import net.jqwik.api.lifecycle.PropertyLifecycleContext;
import net.jqwik.api.lifecycle.TryLifecycleContext;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExecutableInvoker;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestInstances;
import org.junit.jupiter.api.parallel.ExecutionMode;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

class JupiterExtensionContextAdapter implements ExtensionContext {
    private final LifecycleContext context;

    JupiterExtensionContextAdapter(final LifecycleContext context) {
        this.context = context;
    }

    @Override
    public Optional<ExtensionContext> getParent() {
        return Optional.empty();
    }

    @Override
    public ExtensionContext getRoot() {
        // Used in expression evaluation to get store
        return this;
    }

    @Override
    public String getUniqueId() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getDisplayName() {
        return context.label();
    }

    @Override
    public Set<String> getTags() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Optional<AnnotatedElement> getElement() {
        if (context instanceof PropertyLifecycleContext) {
            return Optional.of(((PropertyLifecycleContext) context).targetMethod());
        }
        if (context instanceof TryLifecycleContext) {
            return Optional.of(((TryLifecycleContext) context).targetMethod());
        }
        return context.optionalElement();
    }

    @Override
    public Optional<Class<?>> getTestClass() {
        return context.optionalContainerClass();
    }

    @Override
    public Optional<TestInstance.Lifecycle> getTestInstanceLifecycle() {
        return Optional.empty();
    }

    @Override
    public Optional<Object> getTestInstance() {
        if (context instanceof PropertyLifecycleContext propertyLifecycleContext) {
            return Optional.of(propertyLifecycleContext.testInstance());
        }
        if (context instanceof TryLifecycleContext tryLifecycleContext) {
            return Optional.of(tryLifecycleContext.testInstance());
        }
        return Optional.empty();
    }

    @Override
    public Optional<TestInstances> getTestInstances() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Optional<Method> getTestMethod() {
        if (context instanceof PropertyLifecycleContext propertyLifecycleContext) {
            return Optional.of(propertyLifecycleContext.targetMethod());
        }
        if (context instanceof TryLifecycleContext tryLifecycleContext) {
            return Optional.of(tryLifecycleContext.targetMethod());
        }
        return Optional.empty();
    }

    @Override
    public Optional<Throwable> getExecutionException() {
        return Optional.empty();
    }

    @Override
    public Optional<String> getConfigurationParameter(final String key) {
        return Optional.empty();
    }

    @Override
    public <T> Optional<T> getConfigurationParameter(final String key, final Function<String, T> transformer) {
        return Optional.empty();
    }

    @Override
    public void publishReportEntry(final Map<String, String> map) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Store getStore(final Namespace namespace) {
        return new JupiterStoreAdapter(context);
    }

    @Override
    public ExecutionMode getExecutionMode() {
        return ExecutionMode.SAME_THREAD;
    }

    @Override
    public ExecutableInvoker getExecutableInvoker() {
        return null;
    }
}
