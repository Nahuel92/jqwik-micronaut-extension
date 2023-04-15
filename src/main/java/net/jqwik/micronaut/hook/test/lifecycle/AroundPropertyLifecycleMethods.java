package net.jqwik.micronaut.hook.test.lifecycle;

import jakarta.annotation.*;

import net.jqwik.api.*;
import net.jqwik.api.lifecycle.*;
import net.jqwik.micronaut.extension.*;

public class AroundPropertyLifecycleMethods implements AroundPropertyHook {
	private final JqwikMicronautExtension extension;

	AroundPropertyLifecycleMethods() {
		this.extension = JqwikMicronautExtension.STORE.get();
	}

	@Override
	@NonNullApi
	@Nonnull
	public PropertyExecutionResult aroundProperty(
		final PropertyLifecycleContext context,
		final PropertyExecutor property
	) throws Throwable {
		extension.beforeProperty(context);
		// TODO: Use property.executeAndFinally(..) for better error handling
		final var execute = property.execute();
		extension.afterProperty(context);
		return execute;
	}

	@Override
	public int aroundPropertyProximity() {
        /* Property lifecycle methods (@BeforeProperty, @AfterProperty) use -10.
           Smaller numbers means "further away" from actual invocation of property method.
           -20 is therefore around the lifecycle methods. */
		return -20;
	}
}
