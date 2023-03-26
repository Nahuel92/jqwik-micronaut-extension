package net.jqwik.micronaut.annotation;

import net.jqwik.micronaut.CustomContextBuilder;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE, ElementType.TYPE})
@Documented
@JqwikMicronautTest(contextBuilder = CustomContextBuilder.class)
public @interface CustomBuilderMicronautTest {
}
