package co.com.pragma.usecase.updateapprovedcount;

import co.com.pragma.model.gateways.CustomLogger;
import co.com.pragma.model.report.Report;
import co.com.pragma.model.report.ReportApprovedCountMessage;
import co.com.pragma.model.report.gateways.ReportRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdateApprovedCountUseCaseTest {

    @Mock
    private ReportRepository reportRepository;

    @Mock
    private CustomLogger logger;

    @InjectMocks
    private UpdateApprovedCountUseCase useCase;

    private static final String TOTAL_APPROVED_ID = "total_approved_requests";

    @Test
    void shouldIgnoreMessageWhenStateNotApproved() {
        ReportApprovedCountMessage message = ReportApprovedCountMessage.builder()
                .applicationId(UUID.randomUUID())
                .state("Rejected")
                .amount(500.0)
                .build();

        StepVerifier.create(useCase.incrementApprovedRequestsCount(message))
                .verifyComplete();

        verify(logger).trace("Message with ApplicationId {} ignored. State = {}",
                message.getApplicationId(), "Rejected");
        verifyNoInteractions(reportRepository);
    }

    @Test
    void shouldIncrementApprovedCountWhenStateApproved() {
        ReportApprovedCountMessage message = ReportApprovedCountMessage.builder()
                .applicationId(UUID.randomUUID())
                .state("Approved")
                .amount(1200.0)
                .build();

        Report updatedReport = Report.builder()
                .id(TOTAL_APPROVED_ID)
                .status("Approved")
                .count(10L)
                .totalAmount(1200.0)
                .updatedAt(Instant.now())
                .build();

        when(reportRepository.incrementApprovedCount(TOTAL_APPROVED_ID, "Approved", 1200.0))
                .thenReturn(Mono.just(updatedReport));

        StepVerifier.create(useCase.incrementApprovedRequestsCount(message))
                .verifyComplete();

        verify(reportRepository).incrementApprovedCount(TOTAL_APPROVED_ID, "Approved", 1200.0);
        verify(logger).trace("Counter updated successfully. New value: {}", 10L);
    }

    @Test
    void shouldPropagateErrorWhenRepositoryFails() {
        ReportApprovedCountMessage message = ReportApprovedCountMessage.builder()
                .applicationId(UUID.randomUUID())
                .state("Approved")
                .amount(999.0)
                .build();

        when(reportRepository.incrementApprovedCount(anyString(), anyString(), anyDouble()))
                .thenReturn(Mono.error(new RuntimeException("DB error")));

        StepVerifier.create(useCase.incrementApprovedRequestsCount(message))
                .expectErrorMatches(ex -> ex instanceof RuntimeException &&
                        ex.getMessage().equals("DB error"))
                .verify();

        verify(reportRepository).incrementApprovedCount(TOTAL_APPROVED_ID, "Approved", 999.0);
    }
}