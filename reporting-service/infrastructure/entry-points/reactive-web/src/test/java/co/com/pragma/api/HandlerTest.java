package co.com.pragma.api;

import co.com.pragma.api.dto.ReportDto;
import co.com.pragma.api.mapper.ReportMapper;
import co.com.pragma.model.report.Report;
import co.com.pragma.usecase.getreport.GetReportApprovedByIdUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class HandlerTest {

    @Mock
    private GetReportApprovedByIdUseCase getReportApprovedByIdUseCase;

    @Mock
    private ReportMapper reportMapper;

    @Mock
    private ServerRequest serverRequest;

    @InjectMocks
    private Handler handler;

    private Report report;
    private ReportDto reportDto;

    @BeforeEach
    void setUp() {
        report = Report.builder()
                .id("test-report")
                .status("APPROVED")
                .count(7L)
                .totalAmount(5000.0)
                .build();

        reportDto = ReportDto.builder()
                .id("test-report")
                .status("APPROVED")
                .count(7L)
                .totalAmount(5000.0)
                .build();
    }

    @Test
    void shouldReturnOkWhenReportExists() {
        when(getReportApprovedByIdUseCase.getReportApprovedById())
                .thenReturn(Mono.just(report));
        when(reportMapper.toResponse(report)).thenReturn(reportDto);

        Mono<ServerResponse> responseMono = handler.getReportApprovedById(serverRequest);

        StepVerifier.create(responseMono)
                .consumeNextWith(response -> {
                    assert response.statusCode().is2xxSuccessful();
                    assert response.headers().getContentType().equals(MediaType.APPLICATION_JSON);
                })
                .verifyComplete();
    }

    @Test
    void shouldCompleteWithoutResponseWhenNoReportFound() {
        when(getReportApprovedByIdUseCase.getReportApprovedById())
                .thenReturn(Mono.empty());

        Mono<ServerResponse> responseMono = handler.getReportApprovedById(serverRequest);

        StepVerifier.create(responseMono)
                .verifyComplete();
    }

    @Test
    void shouldReturnErrorWhenUseCaseFails() {
        when(getReportApprovedByIdUseCase.getReportApprovedById())
                .thenReturn(Mono.error(new RuntimeException("DB error")));

        Mono<ServerResponse> responseMono = handler.getReportApprovedById(serverRequest);

        StepVerifier.create(responseMono)
                .expectError(RuntimeException.class)
                .verify();
    }
}
