package net.jqwik.micronaut.extension;

import io.micronaut.context.ApplicationContext;
import io.micronaut.test.context.TestContext;
import net.jqwik.api.Tuple;
import net.jqwik.api.lifecycle.Lifespan;
import net.jqwik.api.lifecycle.RegistrarHook;
import net.jqwik.api.lifecycle.Store;
import net.jqwik.micronaut.annotation.JqwikMicronautTest;
import org.apiguardian.api.API;

import java.lang.reflect.AnnotatedElement;
import java.util.Optional;

@API(status = API.Status.INTERNAL)
public class JqwikMicronautExtension implements RegistrarHook {
    public static TestContext getTestContext(final Class<?> containerClass, final AnnotatedElement annotatedElement) {
        return testContextStore(containerClass, annotatedElement).get();
    }

    private static Store<TestContext> testContextStore(final Class<?> containerClass, final AnnotatedElement annotatedElement) {
        return getOrCreateTestContextStore(containerClass, annotatedElement);
    }

    private static Store<TestContext> getOrCreateTestContextStore(final Class<?> containerClass, final AnnotatedElement annotatedElement) {
        final var build = ApplicationContext.builder().build();

        return Store.getOrCreate(
                storeIdentifier(containerClass),
                Lifespan.RUN,
                () -> new TestContext(
                        build,
                        containerClass,
                        annotatedElement,
                        null,
                        null
                        //context.getTestInstance().orElse(null),
                        //context.getExecutionException().orElse(null)
                )
        );
    }

    private static Tuple.Tuple2<?, ?> storeIdentifier(final Class<?> containerClass) {
        return Tuple.of(JqwikMicronautTest.class, containerClass);
    }

    @Override
    public boolean appliesTo(final Optional<AnnotatedElement> optionalElement) {
        // Only apply to container classes
        return optionalElement.map(element -> element instanceof Class).orElse(false);
    }

    @Override
    public void registerHooks(final Registrar registrar) {
            /*registrar.register(AroundMicronautTestContainer.class, PropagationMode.ALL_DESCENDANTS);
            registrar.register(OutsideLifecycleMethodsHook.class, PropagationMode.ALL_DESCENDANTS);
            registrar.register(InsideLifecycleMethodsHook.class, PropagationMode.ALL_DESCENDANTS);
            registrar.register(ResolveMicronautParametersHook.class, PropagationMode.ALL_DESCENDANTS);
            registrar.register(EnabledIfHook.class, PropagationMode.ALL_DESCENDANTS);
            registrar.register(DisabledIfHook.class, PropagationMode.ALL_DESCENDANTS);*/
    }
}
