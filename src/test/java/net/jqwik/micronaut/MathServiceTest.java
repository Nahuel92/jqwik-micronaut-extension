package net.jqwik.micronaut;

import jakarta.inject.Inject;
import net.jqwik.api.*;
import net.jqwik.micronaut.annotation.JqwikMicronautTest;
import net.jqwik.micronaut.beans.math.MathService;

import static org.assertj.core.api.Assertions.assertThat;

@JqwikMicronautTest
class MathServiceTest {
    @Inject
    private MathService mathService;

    @Property
    void testComputeNumToSquare(@ForAll("10") final Integer num) {
        final Integer result = mathService.compute(num);
        assertThat(result).isEqualTo(num * 4);
    }

    @Provide("10")
    private Arbitrary<Integer> numbers() {
        return Arbitraries.of(10);
    }
}