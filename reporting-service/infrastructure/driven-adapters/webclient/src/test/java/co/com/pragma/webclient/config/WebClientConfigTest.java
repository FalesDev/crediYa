package co.com.pragma.webclient.config;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = WebClientConfig.class)
class WebClientConfigTest {

    @Autowired
    @Qualifier("requestWebClient")
    private WebClient requestWebClient;

    @Autowired
    @Qualifier("authWebClient")
    private WebClient authWebClient;

    private static MockWebServer mockWebServer;

    @BeforeAll
    static void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
    }

    @AfterAll
    static void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        String baseUrl = mockWebServer.url("/").toString();
        registry.add("services.request.url", () -> baseUrl);
        registry.add("services.auth.url", () -> baseUrl);
    }

    @Test
    void requestWebClientIsConfiguredCorrectly() throws InterruptedException {
        mockWebServer.enqueue(new MockResponse()
                .setBody("{\"message\":\"ok\"}")
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        Mono<String> response = requestWebClient.get()
                .uri("/test-path")
                .retrieve()
                .bodyToMono(String.class);

        StepVerifier.create(response)
                .expectNext("{\"message\":\"ok\"}")
                .verifyComplete();

        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertThat(recordedRequest.getPath()).isEqualTo("/test-path");
        assertThat(recordedRequest.getHeader(HttpHeaders.ACCEPT)).isEqualTo(MediaType.APPLICATION_JSON_VALUE);
        assertThat(recordedRequest.getHeader(HttpHeaders.CONTENT_TYPE)).isEqualTo(MediaType.APPLICATION_JSON_VALUE);
    }

    @Test
    void authWebClientIsConfiguredCorrectly() throws InterruptedException {
        mockWebServer.enqueue(new MockResponse().setResponseCode(200));

        authWebClient.post()
                .uri("/auth")
                .retrieve()
                .toBodilessEntity()
                .subscribe();

        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertThat(recordedRequest.getPath()).isEqualTo("/auth");
        assertThat(recordedRequest.getHeader(HttpHeaders.ACCEPT)).isEqualTo(MediaType.APPLICATION_JSON_VALUE);
    }
}
