package co.com.pragma.api.security;

import co.com.pragma.model.exception.TokenValidationException;
import co.com.pragma.model.role.Role;
import co.com.pragma.model.role.gateways.RoleRepository;
import co.com.pragma.model.token.gateways.TokenRepository;
import co.com.pragma.model.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.security.core.context.SecurityContext;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private TokenRepository tokenRepository;

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private User user;
    private Role role;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(UUID.randomUUID())
                .email("test@example.com")
                .idDocument("12345678")
                .idRole(UUID.randomUUID())
                .build();

        role = new Role(user.getIdRole(), "ADMIN", "Administrator");
    }

    @Test
    @DisplayName("Should return empty when Authorization header is missing")
    void noAuthorizationHeader() {
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/test"));

        StepVerifier.create(jwtAuthenticationFilter.load(exchange))
                .verifyComplete();
    }

    @Test
    @DisplayName("Should return empty when Authorization header is not Bearer")
    void invalidAuthorizationHeader() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/test")
                        .header("Authorization", "Basic abc123")
        );

        StepVerifier.create(jwtAuthenticationFilter.load(exchange))
                .verifyComplete();
    }

    @Test
    @DisplayName("Should authenticate user when token is valid and role found")
    void validTokenAndRole() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/test")
                        .header("Authorization", "Bearer valid-token")
        );

        when(tokenRepository.validateToken("valid-token")).thenReturn(Mono.just(user));
        when(roleRepository.findById(user.getIdRole())).thenReturn(Mono.just(role));

        StepVerifier.create(jwtAuthenticationFilter.load(exchange))
                .assertNext(context -> {
                    assertThat(context).isInstanceOf(SecurityContext.class);
                    assertThat(context.getAuthentication().getPrincipal()).isEqualTo(user);
                    assertThat(context.getAuthentication().getAuthorities())
                            .extracting("authority")
                            .containsExactly("ROLE_ADMIN");
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Should return empty and set AUTH_ERROR attribute when token is invalid")
    void invalidToken() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/test")
                        .header("Authorization", "Bearer invalid-token")
        );

        when(tokenRepository.validateToken("invalid-token"))
                .thenReturn(Mono.error(new TokenValidationException("Invalid token")));

        StepVerifier.create(jwtAuthenticationFilter.load(exchange))
                .verifyComplete();

        assertThat(exchange.getAttributes()).containsEntry("AUTH_ERROR", "Invalid token");
    }

    @Test
    @DisplayName("Should return empty when role is not found")
    void roleNotFound() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/test")
                        .header("Authorization", "Bearer valid-token")
        );

        when(tokenRepository.validateToken("valid-token")).thenReturn(Mono.just(user));
        when(roleRepository.findById(user.getIdRole())).thenReturn(Mono.empty());

        StepVerifier.create(jwtAuthenticationFilter.load(exchange))
                .verifyComplete();
    }
}
