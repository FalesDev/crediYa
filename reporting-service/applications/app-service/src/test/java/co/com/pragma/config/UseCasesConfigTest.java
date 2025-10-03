package co.com.pragma.config;

import co.com.pragma.model.auth.gateways.AuthenticationGateway;
import co.com.pragma.model.dailyreport.gateways.DailyReportGateway;
import co.com.pragma.model.gateways.CustomLogger;
import co.com.pragma.model.gateways.RequestValidationGateway;
import co.com.pragma.model.report.gateways.ReportRepository;
import co.com.pragma.usecase.dailyreport.DailyReportUseCase;
import co.com.pragma.usecase.getreport.GetReportApprovedByIdUseCase;
import co.com.pragma.usecase.updateapprovedcount.UpdateApprovedCountUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

public class UseCasesConfigTest {

    @Test
    @DisplayName("Should register all UseCase beans in application context")
    void testAllUseCaseBeansExist() {
        try (AnnotationConfigApplicationContext context =
                     new AnnotationConfigApplicationContext(TestConfig.class)) {

            assertNotNull(context.getBean(DailyReportUseCase.class));
            assertNotNull(context.getBean(GetReportApprovedByIdUseCase.class));
            assertNotNull(context.getBean(UpdateApprovedCountUseCase.class));
        }
    }

    @Configuration
    @Import(UseCasesConfig.class)
    static class TestConfig {
        @Bean
        AuthenticationGateway authenticationGateway() { return mock(AuthenticationGateway.class); }
        @Bean
        RequestValidationGateway requestValidationGateway() { return mock(RequestValidationGateway.class); }
        @Bean
        DailyReportGateway dailyReportGateway() { return mock(DailyReportGateway.class); }
        @Bean
        ReportRepository reportRepository() { return mock(ReportRepository.class); }
        @Bean
        CustomLogger customLogger() { return mock(CustomLogger.class); }
    }
}