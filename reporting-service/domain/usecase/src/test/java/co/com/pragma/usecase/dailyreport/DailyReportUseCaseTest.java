package co.com.pragma.usecase.dailyreport;

import co.com.pragma.model.auth.gateways.AuthenticationGateway;
import co.com.pragma.model.dailyreport.DailyReport;
import co.com.pragma.model.dailyreport.gateways.DailyReportGateway;
import co.com.pragma.model.gateways.CustomLogger;
import co.com.pragma.model.gateways.RequestValidationGateway;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DailyReportUseCaseTest {

    @Mock
    private AuthenticationGateway authenticationGateway;

    @Mock
    private RequestValidationGateway requestGateway;

    @Mock
    private DailyReportGateway dailyReportGateway;

    @Mock
    private CustomLogger logger;

    @InjectMocks
    private DailyReportUseCase useCase;

    @Test
    void shouldExecuteSuccessfully() {
        DailyReport report = DailyReport.builder()
                .approvedLoansCount(10L)
                .totalLoanAmount(50000.0)
                .build();

        when(authenticationGateway.login()).thenReturn(Mono.just("token"));
        when(requestGateway.fetchApprovedRequests("token")).thenReturn(Mono.just(report));
        when(dailyReportGateway.sendDailyReport(report)).thenReturn(Mono.empty());

        StepVerifier.create(useCase.execute())
                .verifyComplete();

        verify(logger).info("Starting daily report generation job");
        verify(logger).trace("Fetched daily report data: {} approved loans.", 10L);
        verify(logger).trace("Daily report successfully sent to SQS queue.");
    }

    @Test
    void shouldFailWhenLoginFails() {
        when(authenticationGateway.login()).thenReturn(Mono.error(new RuntimeException("Login failed")));

        StepVerifier.create(useCase.execute())
                .verifyErrorMatches(ex -> ex.getMessage().equals("Login failed"));

        verify(logger).error(eq("Error during daily report generation: {}"), eq("Login failed"));
    }

    @Test
    void shouldFailWhenFetchRequestsFails() {
        when(authenticationGateway.login()).thenReturn(Mono.just("token"));
        when(requestGateway.fetchApprovedRequests("token"))
                .thenReturn(Mono.error(new RuntimeException("Fetch error")));

        StepVerifier.create(useCase.execute())
                .verifyErrorMatches(ex -> ex.getMessage().equals("Fetch error"));

        verify(logger).error(eq("Error during daily report generation: {}"), eq("Fetch error"));
    }

    @Test
    void shouldFailWhenSendReportFails() {
        DailyReport report = DailyReport.builder()
                .approvedLoansCount(5L)
                .totalLoanAmount(20000.0)
                .build();

        when(authenticationGateway.login()).thenReturn(Mono.just("token"));
        when(requestGateway.fetchApprovedRequests("token")).thenReturn(Mono.just(report));
        when(dailyReportGateway.sendDailyReport(report))
                .thenReturn(Mono.error(new RuntimeException("Send error")));

        StepVerifier.create(useCase.execute())
                .verifyErrorMatches(ex -> ex.getMessage().equals("Send error"));

        verify(logger).error(eq("Error during daily report generation: {}"), eq("Send error"));
    }
}