package co.com.pragma.security.adapter;

import co.com.pragma.model.exception.TokenValidationException;
import co.com.pragma.model.role.Role;
import co.com.pragma.model.role.gateways.RoleRepository;
import co.com.pragma.model.user.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtProviderAdapterTest {

    @Mock
    private RoleRepository roleRepository;

    private JwtProviderAdapter jwtProviderAdapter;

    private User user;
    private Role role;
    private SecretKey secretKey;

    @BeforeEach
    void setup() {
        String secretKeyString = Base64.getEncoder()
                .encodeToString("test-secret-key-12345678901234567890123456789012".getBytes());
        Long expirationTimeInMs = 3600000L;

        jwtProviderAdapter = new JwtProviderAdapter(roleRepository, secretKeyString, expirationTimeInMs);
        secretKey = Keys.hmacShaKeyFor(Base64.getDecoder().decode(secretKeyString));

        user = User.builder()
                .id(UUID.randomUUID())
                .email("test@example.com")
                .idDocument("12345678")
                .idRole(UUID.randomUUID())
                .build();

        role = new Role(user.getIdRole(), "ADMIN", "Admin role");
    }

    @Test
    @DisplayName("Should generate access token when role exists")
    void generateAccessTokenWhenRoleExists() {
        when(roleRepository.findById(user.getIdRole())).thenReturn(Mono.just(role));

        StepVerifier.create(jwtProviderAdapter.generateAccessToken(user))
                .expectNextMatches(token -> token.getAccessToken() != null && token.getExpiresIn() > 0)
                .verifyComplete();
    }

    @Test
    @DisplayName("Should error when role does not exist")
    void generateAccessTokenWhenRoleNotExists() {
        when(roleRepository.findById(user.getIdRole())).thenReturn(Mono.empty());

        StepVerifier.create(jwtProviderAdapter.generateAccessToken(user))
                .expectError()
                .verify();
    }

    @Test
    @DisplayName("Should validate token successfully")
    void validateTokenSuccessfully() {
        String tokenString = Jwts.builder()
                .subject(user.getEmail())
                .claim("idUser", user.getId().toString())
                .claim("idDocument", user.getIdDocument())
                .claim("idRole", user.getIdRole().toString())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 3600000))
                .signWith(secretKey)
                .compact();

        StepVerifier.create(jwtProviderAdapter.validateToken(tokenString))
                .expectNextMatches(u -> u.getEmail().equals(user.getEmail())
                        && u.getId().equals(user.getId())
                        && u.getIdDocument().equals(user.getIdDocument()))
                .verifyComplete();
    }

    @Test
    @DisplayName("Should throw TokenValidationException for invalid signature")
    void validateTokenInvalidSignature() {
        SecretKey differentKey = Keys.hmacShaKeyFor(Base64.getDecoder().decode(
                Base64.getEncoder().encodeToString("different-secret-key-12345678901234567890123456789012".getBytes())));

        String invalidToken = Jwts.builder()
                .subject(user.getEmail())
                .claim("idUser", user.getId().toString())
                .claim("idDocument", user.getIdDocument())
                .claim("idRole", user.getIdRole().toString())
                .claim("role", role.getName())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 3600000))
                .signWith(differentKey)
                .compact();

        StepVerifier.create(jwtProviderAdapter.validateToken(invalidToken))
                .expectErrorSatisfies(throwable -> {
                    assertInstanceOf(TokenValidationException.class, throwable);
                })
                .verify();
    }

    @Test
    @DisplayName("Should throw TokenValidationException for expired token")
    void validateTokenExpired() {
        String expiredToken = Jwts.builder()
                .subject(user.getEmail())
                .claim("idUser", user.getId().toString())
                .claim("idDocument", user.getIdDocument())
                .claim("idRole", user.getIdRole().toString())
                .issuedAt(new Date(System.currentTimeMillis() - 7200000))
                .expiration(new Date(System.currentTimeMillis() - 3600000))
                .signWith(secretKey)
                .compact();

        StepVerifier.create(jwtProviderAdapter.validateToken(expiredToken))
                .expectError(TokenValidationException.class)
                .verify();
    }

    @Test
    @DisplayName("Should throw TokenValidationException for malformed token")
    void validateTokenMalformed() {
        String malformedToken = "this.is.not.a.valid.token";

        StepVerifier.create(jwtProviderAdapter.validateToken(malformedToken))
                .expectError(TokenValidationException.class)
                .verify();
    }

    @Test
    @DisplayName("Should generate token with correct claims")
    void generateTokenWithCorrectClaims() {
        when(roleRepository.findById(user.getIdRole())).thenReturn(Mono.just(role));

        StepVerifier.create(jwtProviderAdapter.generateAccessToken(user))
                .assertNext(token -> {
                    Claims claims = Jwts.parser()
                            .verifyWith(secretKey)
                            .build()
                            .parseSignedClaims(token.getAccessToken())
                            .getPayload();

                    assertEquals(user.getEmail(), claims.getSubject());
                    assertEquals(user.getId().toString(), claims.get("idUser", String.class));
                    assertEquals(user.getIdDocument(), claims.get("idDocument", String.class));
                    assertEquals(user.getIdRole().toString(), claims.get("idRole", String.class));
                    assertEquals(role.getName(), claims.get("role", String.class));
                })
                .verifyComplete();
    }
}
