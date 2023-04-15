package net.jqwik.micronaut.hook.test.lifecycle;

import net.jqwik.api.*;
import net.jqwik.api.lifecycle.*;
import net.jqwik.micronaut.extension.*;

public class BeforeAll implements BeforeContainerHook {
	private final JqwikMicronautExtension extension;

	BeforeAll() {
		this.extension = JqwikMicronautExtension.STORE.get();
	}

	@Override
	@NonNullApi
	public void beforeContainer(final ContainerLifecycleContext context) throws Exception {
		extension.beforeContainer(context);
	}

	@Override
	public int beforeContainerProximity() {
		// Run it before @BeforeContainer methods
		return -20;
	}

}
