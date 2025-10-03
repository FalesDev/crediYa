package co.com.pragma.webclient.adapter;

import co.com.pragma.model.exception.UnauthorizedException;
import co.com.pragma.model.dailyreport.DailyReport;
import co.com.pragma.model.gateways.RequestValidationGateway;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class RequestServiceAdapter implements RequestValidationGateway {

    private final WebClient requestWebClient;

    public RequestServiceAdapter(@Qualifier("requestWebClient") WebClient requestWebClient) {
        this.requestWebClient = requestWebClient;
    }

    @Override
    public Mono<DailyReport> fetchApprovedRequests(String token) {
        return requestWebClient
                .get()
                .uri("/request/api/v1/requests/approved/yesterday")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response -> {
                    if (response.statusCode().equals(HttpStatus.UNAUTHORIZED)) {
                        return Mono.error(new UnauthorizedException("Unauthorized: Invalid token"));
                    }
                    return response.createException().flatMap(Mono::error);
                })
                .bodyToMono(DailyReport.class);
    }
}
