package net.jqwik.micronaut.extension;

import net.jqwik.api.lifecycle.PropagationMode;
import net.jqwik.api.lifecycle.RegistrarHook;
import net.jqwik.micronaut.hook.AroundPropertyMicronaut;
import org.apiguardian.api.API;

@API(status = API.Status.INTERNAL)
public class JqwikMicronautExtension implements RegistrarHook {
    @Override
    public void registerHooks(final RegistrarHook.Registrar registrar) {
        registrar.register(AroundPropertyMicronaut.class, PropagationMode.ALL_DESCENDANTS);
    }
}