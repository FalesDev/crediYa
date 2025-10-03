package co.com.pragma.usecase.registeruser;

import co.com.pragma.model.exception.EmailAlreadyExistsException;
import co.com.pragma.model.exception.EntityNotFoundException;
import co.com.pragma.model.exception.IdDocumentAlreadyExistsException;
import co.com.pragma.model.gateways.CustomLogger;
import co.com.pragma.model.gateways.PasswordHasher;
import co.com.pragma.model.gateways.TransactionManager;
import co.com.pragma.model.role.Role;
import co.com.pragma.model.role.gateways.RoleRepository;
import co.com.pragma.model.user.User;
import co.com.pragma.model.user.gateways.UserRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class RegisterUseCase {

    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;
    private final RoleRepository roleRepository;
    private final TransactionManager transactionManager;
    private final CustomLogger customLogger;

    public Mono<User> register(User user) {
        customLogger.trace("Starting user registration for email: {}", user.getEmail());

        return transactionManager.executeInTransaction(
                validateEmail(user.getEmail())
                        .then(validateIdDocument(user.getIdDocument()))
                        .then(findRoleByName())
                        .map(role -> prepareUser(user, role))
                        .flatMap(userRepository::save)
                        .doOnSuccess(savedUser ->
                                customLogger.trace("User registered successfully: {}", savedUser.getEmail()))
                        .doOnError(error ->
                                customLogger.trace("Registration process failed for {}: {}", user.getEmail(), error.getMessage()))
        );
    }

    private Mono<Void> validateEmail(String email) {
        return userRepository.existsByEmail(email.toLowerCase())
                .flatMap(exists -> {
                    if (exists) {
                        customLogger.trace("Registration failed: Email already exists - {}", email);
                        return Mono.error(new EmailAlreadyExistsException("Email is already registered"));
                    }
                    customLogger.trace("Email {} is available", email);
                    return Mono.empty();
                });
    }

    private Mono<Void> validateIdDocument(String idDocument) {
        return userRepository.existsByIdDocument(idDocument)
                .flatMap(exists -> {
                    if (exists) {
                        customLogger.trace("Registration failed: idDocument already exists - {}", idDocument);
                        return Mono.error(new IdDocumentAlreadyExistsException("IdDocument is already registered"));
                    }
                    customLogger.trace("idDocument {} is available", idDocument);
                    return Mono.empty();
                });
    }

    private Mono<Role> findRoleByName() {
        return roleRepository.findByName("CLIENT")
                .switchIfEmpty(Mono.defer(() -> {
                    customLogger.trace("Role 'CLIENT' not found in database");
                    return Mono.error(new EntityNotFoundException("Role not found"));
                }))
                .doOnSuccess(role -> customLogger.trace("Role 'CLIENT' found"));
    }

    private User prepareUser(User user, Role role) {
        customLogger.trace("Preparing user data for registration");
        user.setEmail(user.getEmail().toLowerCase());
        user.setPassword(passwordHasher.encode(user.getPassword()));
        user.setIdRole(role.getId());
        return user;
    }
}
