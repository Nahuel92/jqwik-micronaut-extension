package net.jqwik.micronaut;

import io.micronaut.context.annotation.Requires;
import io.micronaut.core.util.StringUtils;
import jakarta.inject.Inject;
import net.jqwik.api.Property;
import net.jqwik.micronaut.annotation.JqwikMicronautTest;
import net.jqwik.micronaut.beans.SimpleService;

import static org.assertj.core.api.Assertions.assertThat;

// FIXME: This test fails to pass
@JqwikMicronautTest
@Requires(property = "mockito.test.enabled", defaultValue = StringUtils.FALSE, value = StringUtils.TRUE)
class Simple1Test extends SimpleBaseTest {
    @Inject
    private SimpleService simpleService;

    @Property
    void testComputeNumToSquare() {
        assertThat(simpleService).isNotNull();
    }
}