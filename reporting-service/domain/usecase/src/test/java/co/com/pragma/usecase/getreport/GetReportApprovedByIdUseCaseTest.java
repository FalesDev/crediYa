package co.com.pragma.usecase.getreport;

import co.com.pragma.model.exception.EntityNotFoundException;
import co.com.pragma.model.gateways.CustomLogger;
import co.com.pragma.model.report.Report;
import co.com.pragma.model.report.gateways.ReportRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetReportApprovedByIdUseCaseTest {

    @Mock
    private ReportRepository reportRepository;

    @Mock
    private CustomLogger logger;

    @InjectMocks
    private GetReportApprovedByIdUseCase useCase;

    private static final String TOTAL_APPROVED_ID = "total_approved_requests";

    @Test
    void shouldReturnReportWhenExists() {
        Report report = Report.builder()
                .id(TOTAL_APPROVED_ID)
                .build();

        when(reportRepository.findById(TOTAL_APPROVED_ID)).thenReturn(Mono.just(report));

        StepVerifier.create(useCase.getReportApprovedById())
                .expectNext(report)
                .verifyComplete();

        verify(logger).trace("Report found successfully with id: {}", TOTAL_APPROVED_ID);
    }

    @Test
    void shouldReturnErrorWhenReportNotFound() {
        when(reportRepository.findById(TOTAL_APPROVED_ID)).thenReturn(Mono.empty());

        StepVerifier.create(useCase.getReportApprovedById())
                .expectErrorMatches(ex -> ex instanceof EntityNotFoundException &&
                        ex.getMessage().equals("Report not found"))
                .verify();

        verify(logger).trace(eq("Error searching Report by id: {}, error: {}"),
                eq(TOTAL_APPROVED_ID), eq("Report not found"));
    }

    @Test
    void shouldPropagateErrorWhenRepositoryFails() {
        when(reportRepository.findById(TOTAL_APPROVED_ID))
                .thenReturn(Mono.error(new RuntimeException("DB error")));

        StepVerifier.create(useCase.getReportApprovedById())
                .expectErrorMatches(ex -> ex instanceof RuntimeException &&
                        ex.getMessage().equals("DB error"))
                .verify();

        verify(logger).trace(eq("Error searching Report by id: {}, error: {}"),
                eq(TOTAL_APPROVED_ID), eq("DB error"));
    }
}
