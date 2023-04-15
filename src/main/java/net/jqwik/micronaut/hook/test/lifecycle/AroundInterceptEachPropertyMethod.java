package net.jqwik.micronaut.hook.test.lifecycle;

import jakarta.annotation.*;

import net.jqwik.api.*;
import net.jqwik.api.lifecycle.*;
import net.jqwik.micronaut.extension.*;

public class AroundInterceptEachPropertyMethod implements AroundPropertyHook {
	private final JqwikMicronautExtension extension;

	AroundInterceptEachPropertyMethod() {
		this.extension = JqwikMicronautExtension.STORE.get();
	}

	@Override
	@NonNullApi
	@Nonnull
	public PropertyExecutionResult aroundProperty(PropertyLifecycleContext context, PropertyExecutor property) throws Throwable {
		extension.interceptBeforePropertyMethod(context);
		final var execute = property.execute();
		extension.interceptAfterPropertyMethod(context);
		return execute;
	}

	@Override
	public int aroundPropertyProximity() {
		// Inside AroundPropertyLifecycleMethods but outside @BeforeProperty and @AfterProperty
		return -18;
	}
}
