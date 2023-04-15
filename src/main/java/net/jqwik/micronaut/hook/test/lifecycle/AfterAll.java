package net.jqwik.micronaut.hook.test.lifecycle;

import net.jqwik.api.*;
import net.jqwik.api.lifecycle.*;
import net.jqwik.micronaut.extension.*;

public class AfterAll implements AfterContainerHook {
	private final JqwikMicronautExtension extension;

	AfterAll() {
		this.extension = JqwikMicronautExtension.STORE.get();
	}

	@Override
	@NonNullApi
	public void afterContainer(final ContainerLifecycleContext context) throws Throwable {
		extension.afterContainer(context);
	}

	@Override
	public int afterContainerProximity() {
		// Run it after @AfterContainer methods
		return -20;
	}

}
