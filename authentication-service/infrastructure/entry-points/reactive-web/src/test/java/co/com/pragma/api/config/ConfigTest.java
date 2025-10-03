package co.com.pragma.api.config;

import co.com.pragma.api.security.JwtAuthenticationFilter;
import co.com.pragma.model.gateways.CustomLogger;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.Collections;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@ContextConfiguration(classes = {
        CorsConfig.class,
        SecurityHeadersConfig.class,
        SecurityConfig.class,
        ConfigTest.TestRouter.class
})
@WebFluxTest
class ConfigTest {

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private CustomLogger customLogger;

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private ObjectMapper objectMapper;


    @Test
    @DisplayName("Should return 200 OK with security headers on GET /test")
    void testGetShouldReturnOk() {
        when(jwtAuthenticationFilter.load(any())).thenReturn(Mono.empty());

        webTestClient.get()
                .uri("/test")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueEquals("Content-Security-Policy",
                        "default-src 'self'; frame-ancestors 'self'; form-action 'self'")
                .expectHeader().valueEquals("Strict-Transport-Security", "max-age=31536000;")
                .expectHeader().valueEquals("X-Content-Type-Options", "nosniff")
                .expectHeader().valueEquals("Server", "")
                .expectHeader().valueEquals("Cache-Control", "no-store")
                .expectHeader().valueEquals("Pragma", "no-cache")
                .expectHeader().valueEquals("Referrer-Policy", "strict-origin-when-cross-origin")
                .expectBody(String.class).isEqualTo("ok");
    }

    @Test
    @DisplayName("Should return 204 No Content with security headers on POST /test")
    void testPostShouldReturnNoContent() {
        when(jwtAuthenticationFilter.load(any())).thenReturn(Mono.empty());

        webTestClient.post()
                .uri("/test")
                .exchange()
                .expectStatus().isNoContent()
                .expectHeader().valueEquals("Content-Security-Policy",
                        "default-src 'self'; frame-ancestors 'self'; form-action 'self'")
                .expectHeader().valueEquals("Strict-Transport-Security", "max-age=31536000;")
                .expectHeader().valueEquals("X-Content-Type-Options", "nosniff")
                .expectHeader().valueEquals("Server", "")
                .expectHeader().valueEquals("Cache-Control", "no-store")
                .expectHeader().valueEquals("Pragma", "no-cache")
                .expectHeader().valueEquals("Referrer-Policy", "strict-origin-when-cross-origin");
    }

    @Test
    @DisplayName("Should return 401 Unauthorized when authentication fails")
    void testUnauthorizedWithInvalidToken() {
        when(jwtAuthenticationFilter.load(any())).thenReturn(Mono.empty());

        webTestClient.get()
                .uri("/auth/api/v1/users/123")
                .header("Authorization", "Bearer invalid")
                .exchange()
                .expectStatus().isUnauthorized()
                .expectBody()
                .jsonPath("$.status").isEqualTo(401)
                .jsonPath("$.error").isEqualTo("UNAUTHORIZED")
                .jsonPath("$.message").isEqualTo("Authentication failed");
    }

    @Test
    @DisplayName("Should return 401 Unauthorized with custom AUTH_ERROR message")
    void testUnauthorizedWithCustomAuthError() {
        when(jwtAuthenticationFilter.load(any())).thenAnswer(invocation -> {
            var exchange = invocation.getArgument(0, org.springframework.web.server.ServerWebExchange.class);
            exchange.getAttributes().put("AUTH_ERROR", "Token expired");
            return Mono.empty();
        });

        webTestClient.get()
                .uri("/auth/api/v1/users/123")
                .header("Authorization", "Bearer expired")
                .exchange()
                .expectStatus().isUnauthorized()
                .expectBody()
                .jsonPath("$.status").isEqualTo(401)
                .jsonPath("$.error").isEqualTo("UNAUTHORIZED")
                .jsonPath("$.message").isEqualTo("Token expired");
    }

    @Test
    @DisplayName("Should return 403 Forbidden when user has insufficient roles")
    void testForbiddenWithInsufficientRole() {
        SecurityContext securityContext = new SecurityContextImpl(
                new UsernamePasswordAuthenticationToken(
                        "user", null, Collections.emptyList()
                )
        );

        when(jwtAuthenticationFilter.load(any())).thenReturn(Mono.just(securityContext));

        webTestClient.get()
                .uri("/auth/api/v1/users/123")
                .header("Authorization", "Bearer validButInsufficientRole")
                .exchange()
                .expectStatus().isForbidden()
                .expectBody()
                .jsonPath("$.status").isEqualTo(403)
                .jsonPath("$.error").isEqualTo("FORBIDDEN")
                .jsonPath("$.message").value(msg ->
                        assertThat(msg.toString()).contains("You don't have permission"));
    }

    @Configuration
    static class TestRouter {
        @Bean
        public RouterFunction<ServerResponse> testRoute() {
            return route()
                    .GET("/test", req -> ServerResponse.ok().bodyValue("ok"))
                    .POST("/test", req -> ServerResponse.noContent().build())
                    .GET("/auth/api/v1/users/123", req -> ServerResponse.ok().bodyValue("user details"))
                    .build();
        }
    }
}