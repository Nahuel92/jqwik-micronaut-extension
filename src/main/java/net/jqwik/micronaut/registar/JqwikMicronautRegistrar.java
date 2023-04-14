package net.jqwik.micronaut.registar;

import net.jqwik.api.lifecycle.PropagationMode;
import net.jqwik.api.lifecycle.RegistrarHook;
import net.jqwik.micronaut.hook.Disabled;
import net.jqwik.micronaut.hook.ParameterResolver;
import net.jqwik.micronaut.hook.test.lifecycle.*;
import org.apiguardian.api.API;

@API(status = API.Status.INTERNAL)
public class JqwikMicronautRegistrar implements RegistrarHook {
    @Override
    public void registerHooks(final RegistrarHook.Registrar registrar) {
        registrar.register(BeforeAll.class, PropagationMode.ALL_DESCENDANTS);
        registrar.register(AfterAll.class, PropagationMode.ALL_DESCENDANTS);
        registrar.register(AroundEach.class, PropagationMode.ALL_DESCENDANTS);
        registrar.register(AroundInterceptEach.class, PropagationMode.ALL_DESCENDANTS);
        registrar.register(AroundTestExecution.class, PropagationMode.ALL_DESCENDANTS);
        registrar.register(InterceptEach.class, PropagationMode.ALL_DESCENDANTS);
        registrar.register(ParameterResolver.class, PropagationMode.ALL_DESCENDANTS);
        registrar.register(Disabled.class, PropagationMode.ALL_DESCENDANTS);
    }
}