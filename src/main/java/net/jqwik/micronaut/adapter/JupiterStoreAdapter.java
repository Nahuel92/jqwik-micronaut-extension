package net.jqwik.micronaut.adapter;

import io.micronaut.test.context.TestContext;
import net.jqwik.api.JqwikException;
import net.jqwik.api.lifecycle.LifecycleContext;
import net.jqwik.micronaut.extension.JqwikMicronautExtension;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.util.function.Function;

public class JupiterStoreAdapter implements ExtensionContext.Store {
    private final LifecycleContext context;

    public JupiterStoreAdapter(final LifecycleContext context) {
        this.context = context;
    }

    @Override
    public Object get(final Object key) {
        return JqwikMicronautExtension.getTestContext(
                context.optionalContainerClass().orElseThrow(() -> new JqwikException("No test context registered")),
                context.optionalElement().orElseThrow(() -> new JqwikException("No test context manager registered")
                ));
    }

    @Override
    @SuppressWarnings("unchecked")
    public <V> V get(final Object key, final Class<V> requiredType) {
        if (requiredType.equals(TestContext.class)) {
            return (V) get(key);
        }
        return null;
    }

    @Override
    public <K, V> Object getOrComputeIfAbsent(final K key, final Function<K, V> defaultCreator) {
        return get(key);
    }

    @Override
    public <K, V> V getOrComputeIfAbsent(final K key, Function<K, V> defaultCreator, final Class<V> requiredType) {
        return get(key, requiredType);
    }

    @Override
    public void put(final Object key, final Object value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object remove(final Object key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <V> V remove(final Object key, final Class<V> requiredType) {
        throw new UnsupportedOperationException();
    }
}