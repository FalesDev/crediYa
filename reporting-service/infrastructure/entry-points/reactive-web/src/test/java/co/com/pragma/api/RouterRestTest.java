package co.com.pragma.api;

import co.com.pragma.api.dto.ReportDto;
import co.com.pragma.api.exception.GlobalExceptionHandler;
import co.com.pragma.api.mapper.ReportMapper;
import co.com.pragma.model.exception.EntityNotFoundException;
import co.com.pragma.model.gateways.CustomLogger;
import co.com.pragma.model.report.Report;
import co.com.pragma.usecase.getreport.GetReportApprovedByIdUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@WebFluxTest
@ContextConfiguration(classes = {
        RouterRest.class,
        Handler.class,
        GlobalExceptionHandler.class
})
class RouterRestTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private ApplicationContext context;

    @MockitoBean
    private GetReportApprovedByIdUseCase getReportApprovedByIdUseCase;

    @MockitoBean
    private ReportMapper reportMapper;

    @MockitoBean
    private CustomLogger customLogger;

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


        RouterRest routerRest = context.getBean(RouterRest.class);
        Handler handler = context.getBean(Handler.class);
        GlobalExceptionHandler globalExceptionHandler = context.getBean(GlobalExceptionHandler.class);

        webTestClient = WebTestClient.bindToRouterFunction(routerRest.routerFunction(handler, globalExceptionHandler))
                .build();
    }

    @Test
    void getReportApprovedById_ShouldReturnOkAndReportDto() {
        when(getReportApprovedByIdUseCase.getReportApprovedById()).thenReturn(Mono.just(report));
        when(reportMapper.toResponse(any(Report.class))).thenReturn(reportDto);

        webTestClient.get()
                .uri("/reporting/api/v1/reports")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(ReportDto.class)
                .isEqualTo(reportDto);
    }

    @Test
    void getReportApprovedById_WhenUseCaseReturnsEmpty_ShouldReturnNotFound() {
        when(getReportApprovedByIdUseCase.getReportApprovedById())
                .thenReturn(Mono.error(new EntityNotFoundException("Report not found")));

        webTestClient.get()
                .uri("/reporting/api/v1/reports")
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void getReportApprovedById_WhenUseCaseReturnsError_ShouldReturnServerError() {
        when(getReportApprovedByIdUseCase.getReportApprovedById())
                .thenReturn(Mono.error(new RuntimeException("Internal Server Error")));

        webTestClient.get()
                .uri("/reporting/api/v1/reports")
                .exchange()
                .expectStatus().is5xxServerError();
    }
}
