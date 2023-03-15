package net.jqwik.micronaut;

import io.micronaut.runtime.EmbeddedApplication;
import io.micronaut.test.annotation.MockBean;
import jakarta.inject.Inject;
import net.jqwik.api.Property;
import net.jqwik.micronaut.annotation.JqwikMicronautTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@JqwikMicronautTest
class JqwikMicronautExtensionTest {
    @Inject
    private EmbeddedApplication<?> application;

    @Inject
    private MyObj myObj;

    @MockBean(MyObj.class)
    MyObj myObj() {
        return mock(MyObj.class);
    }

    @Property(tries = 1)
    void testItWorks() {
        assertThat(application.isRunning()).isTrue();
    }

    @Property(tries = 1)
    void testMockWorks() {
        final var newMessage = "Goodbye world!";
        when(myObj.myMethod()).thenReturn(newMessage);

        // then
        assertThat(myObj.myMethod()).isEqualTo(newMessage);
    }
}

