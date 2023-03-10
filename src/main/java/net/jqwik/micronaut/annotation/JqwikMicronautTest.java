package net.jqwik.micronaut.annotation;

import net.jqwik.api.lifecycle.AddLifecycleHook;
import net.jqwik.micronaut.extension.JqwikMicronautExtension;
import org.apiguardian.api.API;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

@AddLifecycleHook(JqwikMicronautExtension.class)
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@API(status = EXPERIMENTAL, since = "TBD")
public @interface JqwikMicronautTest {
}
