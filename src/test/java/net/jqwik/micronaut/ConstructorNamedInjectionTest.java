package net.jqwik.micronaut;

import jakarta.inject.Named;
import net.jqwik.api.Property;
import net.jqwik.micronaut.annotation.JqwikMicronautTest;
import net.jqwik.micronaut.beans.MyInterface;

@JqwikMicronautTest
class ConstructorNamedInjectionTest {
    private final MyInterface myInterface;

    ConstructorNamedInjectionTest(@Named("B") final MyInterface myInterface) {
        this.myInterface = myInterface;
    }

    @Property
    void testConstructorInjected() {
        assertThat(myInterface.test()).isEqualTo("B");
    }
}

