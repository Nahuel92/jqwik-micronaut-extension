package net.jqwik.micronaut.hook.test.lifecycle;

import jakarta.annotation.*;

import net.jqwik.api.*;
import net.jqwik.api.lifecycle.*;
import net.jqwik.micronaut.extension.*;

// TODO: Maybe this should be an AroundTryHook?
public class AroundPropertyExecution implements AroundPropertyHook {
	private final JqwikMicronautExtension extension;

	AroundPropertyExecution() {
		this.extension = JqwikMicronautExtension.STORE.get();
	}

	@Override
	@NonNullApi
	@Nonnull
	public PropertyExecutionResult aroundProperty(
		final PropertyLifecycleContext context,
		final PropertyExecutor property
	) throws Throwable {
		extension.beforePropertyExecution(context);
        return property.executeAndFinally(
            () -> extension.afterPropertyExecution(context)
        );
	}

	@Override
	public int aroundPropertyProximity() {
		// In-between @BeforeProperty, @AfterProperty and actual property execution
		return -5;
	}
}
