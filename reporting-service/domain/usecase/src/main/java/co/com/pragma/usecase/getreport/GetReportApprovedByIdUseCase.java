package co.com.pragma.usecase.getreport;

import co.com.pragma.model.exception.EntityNotFoundException;
import co.com.pragma.model.gateways.CustomLogger;
import co.com.pragma.model.report.Report;
import co.com.pragma.model.report.gateways.ReportRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class GetReportApprovedByIdUseCase {

    private final ReportRepository reportRepository;
    private final CustomLogger logger;

    private static final String TOTAL_APPROVED_ID = "total_approved_requests";

    public Mono<Report> getReportApprovedById() {
        return reportRepository.findById(TOTAL_APPROVED_ID)
                .switchIfEmpty(Mono.error(new EntityNotFoundException("Report not found")))
                .doOnSuccess(report -> logger.trace("Report found successfully with id: {}", TOTAL_APPROVED_ID))
                .doOnError(error -> logger.trace("Error searching Report by id: {}, error: {}", TOTAL_APPROVED_ID, error.getMessage()));
    }
}
