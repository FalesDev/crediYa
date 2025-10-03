package co.com.pragma.config;

import co.com.pragma.model.gateways.CustomLogger;
import co.com.pragma.model.gateways.PasswordHasher;
import co.com.pragma.model.role.Role;
import co.com.pragma.model.role.gateways.RoleRepository;
import co.com.pragma.model.user.User;
import co.com.pragma.model.user.gateways.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class DataInitializer {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordHasher passwordHasher;
    private final CustomLogger customLogger;

    @EventListener(ApplicationReadyEvent.class)
    public void initializeData() {
        Mono<Role> adminRole = createRoleIfNotFound("ADMIN","Administrator with full access");
        Mono<Role> adviserRole = createRoleIfNotFound("ADVISER","Advisor with limited access");
        Mono<Role> clientRole = createRoleIfNotFound("CLIENT","Client with access to own resources only");
        Mono<Role> reportRole = createRoleIfNotFound("REPORT_JOB", "Role for scheduled reporting jobs");

        Mono.when(adminRole, adviserRole, clientRole, reportRole)
                .then(adminRole.flatMap(role -> createUserIfNotFound(
                        "Admin", "admin@test.com", "11111111", "987654321", role, "adminpassword")))
                .then(adviserRole.flatMap(role -> createUserIfNotFound(
                        "Adviser", "adviser@test.com", "11111112", "987654322", role, "adviserpassword")))
                .then(clientRole.flatMap(role -> createUserIfNotFound(
                        "Client", "client@test.com", "11111113", "987654323", role, "clientpassword")))
                .then(reportRole.flatMap(role -> createUserIfNotFound(
                        "Report", "report-service@test.com", "11111114", "987654366", role, "reportpassword")))
                .doOnError(err -> customLogger.error("Data initialization failed: {}", err.getMessage()))
                .subscribe();
    }

    private Mono<Role> createRoleIfNotFound(String name, String description) {
        return roleRepository.findByName(name)
                .switchIfEmpty(Mono.defer(() -> {
                    customLogger.trace("Creating role: {}", name);
                    Role role = Role.builder()
                            .name(name)
                            .description(description)
                            .build();
                    return roleRepository.save(role);
                }));
    }

    private Mono<User> createUserIfNotFound(String firstName, String email,
                                            String idDocument, String phoneNumber,
                                            Role role, String rawPassword) {
        return userRepository.findByEmail(email)
                .switchIfEmpty(Mono.defer(() -> {
                    customLogger.trace("Creating user: {}", email);
                    User user = User.builder()
                            .firstName(firstName)
                            .lastName("User")
                            .email(email)
                            .idDocument(idDocument)
                            .phoneNumber(phoneNumber)
                            .idRole(role.getId())
                            .baseSalary(5000.0)
                            .password(passwordHasher.encode(rawPassword))
                            .build();
                    return userRepository.save(user);
                }));
    }
}
