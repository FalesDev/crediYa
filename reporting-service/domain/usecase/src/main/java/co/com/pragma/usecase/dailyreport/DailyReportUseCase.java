package co.com.pragma.usecase.dailyreport;

import co.com.pragma.model.auth.gateways.AuthenticationGateway;
import co.com.pragma.model.dailyreport.gateways.DailyReportGateway;
import co.com.pragma.model.gateways.CustomLogger;
import co.com.pragma.model.gateways.RequestValidationGateway;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class DailyReportUseCase {

    private final AuthenticationGateway authenticationGateway;
    private final RequestValidationGateway requestGateway;
    private final DailyReportGateway dailyReportGateway;
    private final CustomLogger logger;

    public Mono<Void> execute() {
        logger.info("Starting daily report generation job");
        return authenticationGateway.login()
                .flatMap(requestGateway::fetchApprovedRequests)
                .doOnNext(report -> logger.trace("Fetched daily report data: {} approved loans.", report.getApprovedLoansCount()))
                .flatMap(dailyReportGateway::sendDailyReport)
                .doOnSuccess(v -> logger.trace("Daily report successfully sent to SQS queue."))
                .doOnError(error -> logger.error("Error during daily report generation: {}", error.getMessage()))
                .then();
    }
}
