package co.com.pragma.usecase.updateapprovedcount;

import co.com.pragma.model.gateways.CustomLogger;
import co.com.pragma.model.report.ReportApprovedCountMessage;
import co.com.pragma.model.report.gateways.ReportRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class UpdateApprovedCountUseCase {

    private final ReportRepository reportRepository;
    private final CustomLogger logger;

    private static final String TOTAL_APPROVED_ID = "total_approved_requests";

    public Mono<Void> incrementApprovedRequestsCount(ReportApprovedCountMessage message) {

        if (!"Approved".equalsIgnoreCase(message.getState())) {
            logger.trace("Message with ApplicationId {} ignored. State = {}", message.getApplicationId(), message.getState());
            return Mono.empty();
        }

        return reportRepository.incrementApprovedCount(
                        TOTAL_APPROVED_ID,
                        message.getState(),
                        message.getAmount()
                )
                .doOnSuccess(report -> logger.trace("Counter updated successfully. New value: {}", report.getCount()))
                .then();
    }
}
