package co.com.pragma.model.gateways;

import co.com.pragma.model.dailyreport.DailyReport;
import reactor.core.publisher.Mono;

public interface RequestValidationGateway {
    Mono<DailyReport> fetchApprovedRequests(String token);
}
