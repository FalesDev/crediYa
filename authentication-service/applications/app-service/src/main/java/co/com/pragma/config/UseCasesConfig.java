package co.com.pragma.config;

import co.com.pragma.model.gateways.CustomLogger;
import co.com.pragma.model.gateways.PasswordHasher;
import co.com.pragma.model.gateways.TransactionManager;
import co.com.pragma.model.role.gateways.RoleRepository;
import co.com.pragma.model.token.gateways.TokenRepository;
import co.com.pragma.model.user.gateways.UserRepository;
import co.com.pragma.usecase.findrolebyid.FindRoleByIdUseCase;
import co.com.pragma.usecase.finduserbyiddocument.FindUserByIdDocumentUseCase;
import co.com.pragma.usecase.findusersbyid.FindUsersByIdUseCase;
import co.com.pragma.usecase.login.LoginUseCase;
import co.com.pragma.usecase.registeruser.RegisterUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;

@Configuration
@ComponentScan(basePackages = "co.com.pragma.usecase",
        includeFilters = {
                @ComponentScan.Filter(type = FilterType.REGEX, pattern = "^.+UseCase$")
        },
        useDefaultFilters = false)
public class UseCasesConfig {

    @Bean
    RegisterUseCase registerUseCase(
            UserRepository userRepository,
            PasswordHasher passwordHasher,
            RoleRepository roleRepository,
            TransactionManager transactionManager,
            CustomLogger customLogger
    ) {
        return new RegisterUseCase(userRepository, passwordHasher, roleRepository, transactionManager,customLogger);
    }

    @Bean
    LoginUseCase loginUseCase(
            UserRepository userRepository,
            PasswordHasher passwordHasher,
            TokenRepository tokenRepository,
            CustomLogger customLogger
    ) {
        return new LoginUseCase(userRepository, passwordHasher, tokenRepository,customLogger);
    }

    @Bean
    FindUserByIdDocumentUseCase findUserByIdDocumentUseCase(
            UserRepository userRepository,
            CustomLogger customLogger
    ) {
        return new FindUserByIdDocumentUseCase(userRepository,customLogger);
    }

    @Bean
    FindRoleByIdUseCase findRoleByIdUseCase(
            RoleRepository roleRepository,
            CustomLogger customLogger
    ) {
        return new FindRoleByIdUseCase(roleRepository,customLogger);
    }

    @Bean
    FindUsersByIdUseCase findUsersByIdUseCase(
            UserRepository userRepository,
            CustomLogger customLogger
    ) {
        return new FindUsersByIdUseCase(userRepository,customLogger);
    }
}
