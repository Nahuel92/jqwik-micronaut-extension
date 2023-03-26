package net.jqwik.micronaut;

import io.micronaut.context.DefaultApplicationContextBuilder;
import io.micronaut.core.annotation.Introspected;

import java.util.Map;

@Introspected
class CustomContextBuilder extends DefaultApplicationContextBuilder {
    CustomContextBuilder() {
        properties(Map.of("custom.builder.prop", "value"));
    }
}
