package org.apache.http.spring.boot;

import org.apache.http.message.BasicHttpRequest;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.impl.client.CloseableHttpClient;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;

class HttpClientHeaderConfigurationTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withUserConfiguration(PropertiesConfiguration.class);

    @Test
    void bindsConfiguredHeadersAndAddsThemToRequests() {
        runner.withPropertyValues("httpclient.headers.[X-Trace-Id]=trace-123")
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    HttpClientProperties properties = context.getBean(HttpClientProperties.class);
                    assertThat(properties.getHeaders()).containsEntry("X-Trace-Id", "trace-123");

                    BasicHttpRequest request = new BasicHttpRequest("GET", "/");
                    new HttpClientDependsOnAutoConfiguration().headerInterceptor(properties)
                            .process(request, new BasicHttpContext());
                    assertThat(request.getFirstHeader("X-Trace-Id").getValue()).isEqualTo("trace-123");
                });
    }

    @Test
    void leavesRequestsUnchangedWithoutConfiguredHeaders() {
        runner.run(context -> {
            assertThat(context).hasNotFailed();
            HttpClientProperties properties = context.getBean(HttpClientProperties.class);
            assertThat(properties.getHeaders()).isEmpty();

            BasicHttpRequest request = new BasicHttpRequest("GET", "/");
            new HttpClientDependsOnAutoConfiguration().headerInterceptor(properties)
                    .process(request, new BasicHttpContext());
            assertThat(request.getAllHeaders()).isEmpty();
        });
    }

    @Test
    void starterRegistersAUsableClientWithoutManualConfiguration() {
        new ApplicationContextRunner().withUserConfiguration(AutoConfigurationApplication.class)
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    assertThat(context).hasSingleBean(CloseableHttpClient.class);
                });
    }

    @Configuration
    @EnableConfigurationProperties(HttpClientProperties.class)
    static class PropertiesConfiguration {
    }

    @Configuration
    @EnableAutoConfiguration
    static class AutoConfigurationApplication {
    }
}
