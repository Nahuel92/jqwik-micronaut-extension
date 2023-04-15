package net.jqwik.micronaut.hook.test.lifecycle;

import net.jqwik.api.lifecycle.*;
import net.jqwik.micronaut.extension.*;

public class InterceptAfterPropertyMethod {
	public static class Pre implements AroundPropertyHook {
		private final JqwikMicronautExtension micronautExtension;

		public Pre() {
			micronautExtension = JqwikMicronautExtension.STORE.get();
		}

		@Override
		public PropertyExecutionResult aroundProperty(PropertyLifecycleContext context, PropertyExecutor property) throws Throwable {
			return property.executeAndFinally(
				() -> micronautExtension.preAfterPropertyMethod(context)
			);
		}

		@Override
		public int aroundPropertyProximity() {
			return -9;
		}
	}

	public static class Post implements AroundPropertyHook {

		private final JqwikMicronautExtension micronautExtension;

		public Post() {
			micronautExtension = JqwikMicronautExtension.STORE.get();
		}

		@Override
		public PropertyExecutionResult aroundProperty(PropertyLifecycleContext context, PropertyExecutor property) throws Throwable {
			return property.executeAndFinally(
				() -> micronautExtension.postAfterPropertyMethod(context)
			);
		}

		@Override
		public int aroundPropertyProximity() {
			return -11;
		}
	}
}
