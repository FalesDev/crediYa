package co.com.pragma.model.dailyreport.gateways;

import co.com.pragma.model.dailyreport.DailyReport;
import reactor.core.publisher.Mono;

public interface DailyReportGateway {
    Mono<Void> sendDailyReport(DailyReport payload);
}
