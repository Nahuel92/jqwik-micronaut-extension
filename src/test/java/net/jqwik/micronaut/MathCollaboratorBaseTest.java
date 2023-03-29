package net.jqwik.micronaut;

import io.micronaut.context.annotation.Requires;
import io.micronaut.core.util.StringUtils;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.test.annotation.MockBean;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import net.jqwik.api.Property;
import net.jqwik.micronaut.annotation.JqwikMicronautTest;
import net.jqwik.micronaut.beans.math.MathService;
import net.jqwik.micronaut.beans.math.MathServiceImpl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

// FIXME: This test fails to pass (can't find and inject HttpClient)
@JqwikMicronautTest
@Requires(property = "mockito.test.enabled", defaultValue = StringUtils.FALSE, value = StringUtils.TRUE)
class MathCollaboratorBaseTest extends MathBaseTest {
    @Inject
    private MathService mathService;

    @Inject
    @Client("/")
    private HttpClient client;

    @Property
    void testComputeNumToSquare() {
        when(mathService.compute(10))
                .then(invocation -> Long.valueOf(Math.round(Math.pow(2, 2))).intValue());

        final Integer result = client.toBlocking().retrieve(HttpRequest.GET("/math/compute/10"), Integer.class);

        assertThat(result).isEqualTo((Integer) 4);
        verify(mathService).compute(10);
    }
}

abstract class MathBaseTest {
    @MockBean(MathServiceImpl.class)
    MathService mathService() {
        return mock(MathService.class);
    }
}

@Controller("/math")
class MathController {
    private final MathService mathService;

    MathController(@Named("MathServiceImpl") final MathService mathService) {
        this.mathService = mathService;
    }

    @Get(uri = "/compute/{number}", processes = MediaType.TEXT_PLAIN)
    String compute(final Integer number) {
        return String.valueOf(mathService.compute(number));
    }
}