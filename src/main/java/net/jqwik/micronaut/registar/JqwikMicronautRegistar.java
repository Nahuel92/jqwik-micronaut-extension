package net.jqwik.micronaut.registar;

import net.jqwik.api.lifecycle.PropagationMode;
import net.jqwik.api.lifecycle.RegistrarHook;
import net.jqwik.micronaut.hook.*;
import org.apiguardian.api.API;

@API(status = API.Status.INTERNAL)
public class JqwikMicronautRegistar implements RegistrarHook {
    @Override
    public void registerHooks(final RegistrarHook.Registrar registrar) {
        registrar.register(BeforeMicronautContainerHook.class, PropagationMode.ALL_DESCENDANTS);
        registrar.register(AfterMicronautContainer.class, PropagationMode.ALL_DESCENDANTS);
        registrar.register(AroundPropertyMicronaut.class, PropagationMode.ALL_DESCENDANTS);
        registrar.register(AroundInterceptor.class, PropagationMode.ALL_DESCENDANTS);
        registrar.register(TestInterceptor.class, PropagationMode.ALL_DESCENDANTS);
        registrar.register(ParameterResolver.class, PropagationMode.ALL_DESCENDANTS);
        registrar.register(DisabledHook.class, PropagationMode.ALL_DESCENDANTS);
    }
}