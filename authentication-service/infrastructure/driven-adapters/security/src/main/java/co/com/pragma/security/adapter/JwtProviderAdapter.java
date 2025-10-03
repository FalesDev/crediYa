package co.com.pragma.security.adapter;

import co.com.pragma.model.exception.TokenValidationException;
import co.com.pragma.model.role.gateways.RoleRepository;
import co.com.pragma.model.token.Token;
import co.com.pragma.model.token.gateways.TokenRepository;
import co.com.pragma.model.user.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import javax.management.relation.RoleNotFoundException;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;


@Component
public class JwtProviderAdapter implements TokenRepository {

    private final RoleRepository roleRepository;
    private final Long expirationTimeInMs;
    private final SecretKey secretKey;

    public JwtProviderAdapter(
            RoleRepository roleRepository,
            @Value("${jwt.secret}") String secretKeyString,
            @Value("${jwt.expiration-ms}") long expirationTimeInMs
    ) {
        this.roleRepository = roleRepository;
        this.expirationTimeInMs = expirationTimeInMs;
        this.secretKey = Keys.hmacShaKeyFor(Base64.getDecoder().decode(secretKeyString));
    }

    @Override
    public Mono<Token> generateAccessToken(User user) {
        return roleRepository.findById(user.getIdRole())
                .switchIfEmpty(Mono.error(new RoleNotFoundException()))
                .map(role -> {
                    String tokenString = Jwts.builder()
                            .subject(user.getEmail())
                            .claim("idUser", user.getId().toString())
                            .claim("idDocument", user.getIdDocument())
                            .claim("idRole", user.getIdRole().toString())
                            .claim("role", role.getName())
                            .issuedAt(new Date())
                            .expiration(new Date(System.currentTimeMillis() + expirationTimeInMs))
                            .signWith(secretKey)
                            .compact();

                    long expiresIn = expirationTimeInMs / 1000;

                    return new Token(tokenString, expiresIn);
                });
    }

    @Override
    public Mono<User> validateToken(String token) {
        return Mono.fromSupplier(() -> {
            try {
                Claims claims = Jwts.parser()
                        .verifyWith(secretKey)
                        .build()
                        .parseSignedClaims(token)
                        .getPayload();

                return User.builder()
                        .id(UUID.fromString(claims.get("idUser", String.class)))
                        .email(claims.getSubject())
                        .idDocument(claims.get("idDocument", String.class))
                        .idRole(UUID.fromString(claims.get("idRole", String.class)))
                        .build();
            } catch (SignatureException ex) {
                throw new TokenValidationException("Invalid JWT signature");
            } catch (ExpiredJwtException ex) {
                throw new TokenValidationException("JWT token expired");
            } catch (JwtException ex) {
                throw new TokenValidationException("Invalid JWT token: " + ex.getMessage());
            }
        });
    }
}
