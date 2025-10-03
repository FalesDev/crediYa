package co.com.pragma.config;

import co.com.pragma.model.auth.gateways.AuthenticationGateway;
import co.com.pragma.model.dailyreport.gateways.DailyReportGateway;
import co.com.pragma.model.gateways.CustomLogger;
import co.com.pragma.model.gateways.RequestValidationGateway;
import co.com.pragma.model.report.gateways.ReportRepository;
import co.com.pragma.usecase.dailyreport.DailyReportUseCase;
import co.com.pragma.usecase.getreport.GetReportApprovedByIdUseCase;
import co.com.pragma.usecase.updateapprovedcount.UpdateApprovedCountUseCase;
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
    DailyReportUseCase dailyReportUseCase(
            AuthenticationGateway authenticationGateway,
            RequestValidationGateway requestGateway,
            DailyReportGateway dailyReportGateway,
            CustomLogger customLogger
    ) {
        return new DailyReportUseCase(authenticationGateway, requestGateway,
                dailyReportGateway,customLogger);
    }

    @Bean
    GetReportApprovedByIdUseCase getReportApprovedByIdUseCase(
            ReportRepository reportRepository,
            CustomLogger customLogger
    ) {
        return new GetReportApprovedByIdUseCase(reportRepository, customLogger);
    }

    @Bean
    UpdateApprovedCountUseCase updateApprovedCountUseCase(
            ReportRepository reportRepository,
            CustomLogger customLogger
    ) {
        return new UpdateApprovedCountUseCase(reportRepository, customLogger);
    }
}
