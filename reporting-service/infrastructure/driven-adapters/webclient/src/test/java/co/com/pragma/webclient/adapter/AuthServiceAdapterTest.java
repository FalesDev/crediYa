package co.com.pragma.webclient.adapter;

import co.com.pragma.model.auth.TokenResponse;
import co.com.pragma.model.exception.UnauthorizedException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;

class AuthServiceAdapterTest {

    private static MockWebServer mockWebServer;
    private AuthServiceAdapter authServiceAdapter;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeAll
    static void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
    }

    @AfterAll
    static void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @BeforeEach
    void initialize() {
        String baseUrl = String.format("http://localhost:%s", mockWebServer.getPort());
        WebClient testWebClient = WebClient.builder()
                .baseUrl(baseUrl)
                .build();

        authServiceAdapter = new AuthServiceAdapter(testWebClient, "test@email.com", "test-password");
    }

    @Test
    void loginShouldReturnTokenOnSuccess() throws JsonProcessingException {
        String expectedToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...";
        TokenResponse tokenResponse = new TokenResponse();
        tokenResponse.setAccessToken(expectedToken);

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(objectMapper.writeValueAsString(tokenResponse))
        );

        Mono<String> resultMono = authServiceAdapter.login();

        StepVerifier.create(resultMono)
                .expectNext(expectedToken)
                .verifyComplete();
    }

    @Test
    void loginShouldThrowUnauthorizedExceptionOn401() {
        mockWebServer.enqueue(new MockResponse().setResponseCode(401));

        Mono<String> resultMono = authServiceAdapter.login();

        StepVerifier.create(resultMono)
                .expectError(UnauthorizedException.class)
                .verify();
    }

    @Test
    void loginShouldThrowWebClientResponseExceptionOn400() {
        mockWebServer.enqueue(new MockResponse().setResponseCode(400));

        Mono<String> resultMono = authServiceAdapter.login();

        StepVerifier.create(resultMono)
                .expectError(WebClientResponseException.BadRequest.class)
                .verify();
    }

    @Test
    void loginShouldThrowWebClientResponseExceptionOn500() {
        mockWebServer.enqueue(new MockResponse().setResponseCode(500));

        Mono<String> resultMono = authServiceAdapter.login();

        StepVerifier.create(resultMono)
                .expectError(WebClientResponseException.InternalServerError.class)
                .verify();
    }
}
