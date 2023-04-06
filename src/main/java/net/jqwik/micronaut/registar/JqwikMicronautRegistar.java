package net.jqwik.micronaut.registar;

import net.jqwik.api.lifecycle.PropagationMode;
import net.jqwik.api.lifecycle.RegistrarHook;
import net.jqwik.micronaut.hook.AroundPropertyMicronaut;
import net.jqwik.micronaut.hook.BeforeMicronautContainerHook;
import net.jqwik.micronaut.hook.DisabledHook;
import net.jqwik.micronaut.hook.ParameterResolver;
import org.apiguardian.api.API;

@API(status = API.Status.INTERNAL)
public class JqwikMicronautRegistar implements RegistrarHook {
    @Override
    public void registerHooks(final RegistrarHook.Registrar registrar) {
        registrar.register(BeforeMicronautContainerHook.class, PropagationMode.ALL_DESCENDANTS);
        registrar.register(AroundPropertyMicronaut.class, PropagationMode.ALL_DESCENDANTS);
        registrar.register(ParameterResolver.class, PropagationMode.ALL_DESCENDANTS);
        registrar.register(DisabledHook.class, PropagationMode.ALL_DESCENDANTS);
        //registrar.register(BeforePropertyHook.class, PropagationMode.ALL_DESCENDANTS);
        //registrar.register(AfterPropertyHook.class, PropagationMode.ALL_DESCENDANTS);
        //registrar.register(AroundTryHook.class, PropagationMode.ALL_DESCENDANTS);
    }
}