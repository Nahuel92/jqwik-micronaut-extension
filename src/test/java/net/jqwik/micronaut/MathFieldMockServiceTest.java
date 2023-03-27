package net.jqwik.micronaut;

import io.micronaut.test.annotation.MockBean;
import jakarta.inject.Inject;
import net.jqwik.api.Example;
import net.jqwik.micronaut.annotation.JqwikMicronautTest;
import net.jqwik.micronaut.beans.math.MathService;
import net.jqwik.micronaut.beans.math.MathServiceImpl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@JqwikMicronautTest
class MathFieldMockServiceTest {
    @MockBean(MathServiceImpl.class)
    private final MathService mock = mock(MathService.class);
    @Inject
    private MathService mathService;

    @Example
    void testComputeNumToSquare() {
        when(mathService.compute(10))
                .then(invocation -> Long.valueOf(Math.round(Math.pow(10, 2))).intValue());

        final Integer result = mathService.compute(10);

        assertThat(result).isEqualTo(100);
        verify(mathService).compute(10);
    }
}