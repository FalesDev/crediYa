package co.com.pragma.api.security;

import co.com.pragma.model.exception.TokenValidationException;
import co.com.pragma.model.role.gateways.RoleRepository;
import co.com.pragma.model.token.gateways.TokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Collections;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter implements ServerSecurityContextRepository {

    private final TokenRepository tokenRepository;
    private final RoleRepository roleRepository;


    @Override
    public Mono<Void> save(ServerWebExchange exchange, SecurityContext context) {
        return Mono.empty();
    }

    @Override
    public Mono<SecurityContext> load(ServerWebExchange exchange) {
        return Mono.justOrEmpty(exchange.getRequest().getHeaders().getFirst("Authorization"))
                .filter(authHeader -> authHeader.startsWith("Bearer "))
                .map(authHeader -> authHeader.substring(7))
                .flatMap(token -> tokenRepository.validateToken(token)
                        .onErrorResume(TokenValidationException.class, ex -> {
                            exchange.getAttributes().put("AUTH_ERROR", ex.getMessage());
                            return Mono.empty();
                        })
                )
                .flatMap(user -> roleRepository.findById(user.getIdRole())
                        .map(role -> {
                            var authorities = Collections.singletonList(
                                    new SimpleGrantedAuthority("ROLE_" + role.getName())
                            );

                            return new UsernamePasswordAuthenticationToken(
                                    user,
                                    null,
                                    authorities
                            );
                        })
                )
                .map(SecurityContextImpl::new);
    }
}
