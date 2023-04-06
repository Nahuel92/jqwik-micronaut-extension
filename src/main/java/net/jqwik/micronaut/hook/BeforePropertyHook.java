package net.jqwik.micronaut.hook;

/*
public class BeforePropertyHook implements AroundPropertyHook {
    @Override
    @NonNullApi
    @Nonnull
    public PropertyExecutionResult aroundProperty(final PropertyLifecycleContext context,
                                                  final PropertyExecutor property) throws Throwable {
        final var testContext = JqwikMicronautExtension.EXTENSION_STORE.get().testContext(context);
        JqwikMicronautExtension.EXTENSION_STORE.get().beforeSetupTest(testContext);
        return ;
    }

    @Override
    public int aroundPropertyProximity() {
        return -15;
    }
}*/
