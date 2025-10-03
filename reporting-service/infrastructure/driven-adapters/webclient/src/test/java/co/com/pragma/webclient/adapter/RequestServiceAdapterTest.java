package co.com.pragma.webclient.adapter;

import co.com.pragma.model.dailyreport.DailyReport;
import co.com.pragma.model.exception.UnauthorizedException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;

class RequestServiceAdapterTest {

    private static MockWebServer mockWebServer;
    private RequestServiceAdapter requestServiceAdapter;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeAll
    static void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
    }

    @AfterAll
    static void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @BeforeEach
    void initialize() {
        String baseUrl = String.format("http://localhost:%s", mockWebServer.getPort());
        WebClient testWebClient = WebClient.builder()
                .baseUrl(baseUrl)
                .build();

        requestServiceAdapter = new RequestServiceAdapter(testWebClient);
    }

    @Test
    void fetchApprovedRequestsShouldReturnDailyReportOnSuccess() throws JsonProcessingException {
        DailyReport expectedReport = DailyReport.builder()
                .approvedLoansCount(10L)
                .totalLoanAmount(150000.50)
                .build();

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(objectMapper.writeValueAsString(expectedReport))
        );

        Mono<DailyReport> resultMono = requestServiceAdapter.fetchApprovedRequests("fake-token");

        StepVerifier.create(resultMono)
                .expectNextMatches(report ->
                        report.getApprovedLoansCount().equals(10L) &&
                                report.getTotalLoanAmount().equals(150000.50)
                )
                .verifyComplete();
    }

    @Test
    void fetchApprovedRequestsShouldThrowUnauthorizedExceptionOn401() {
        mockWebServer.enqueue(new MockResponse().setResponseCode(401));

        Mono<DailyReport> resultMono = requestServiceAdapter.fetchApprovedRequests("invalid-token");

        StepVerifier.create(resultMono)
                .expectError(UnauthorizedException.class)
                .verify();
    }

    @Test
    void fetchApprovedRequestsShouldThrowWebClientExceptionOn403() {
        mockWebServer.enqueue(new MockResponse().setResponseCode(403));

        Mono<DailyReport> resultMono = requestServiceAdapter.fetchApprovedRequests("forbidden-token");

        StepVerifier.create(resultMono)
                .expectError(WebClientResponseException.Forbidden.class)
                .verify();
    }
}
