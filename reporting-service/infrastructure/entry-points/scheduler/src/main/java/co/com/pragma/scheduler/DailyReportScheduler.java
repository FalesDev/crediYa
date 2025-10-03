package co.com.pragma.scheduler;

import co.com.pragma.usecase.dailyreport.DailyReportUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DailyReportScheduler {

    private final DailyReportUseCase dailyReportUseCase;

    @Scheduled(cron = "${scheduler.daily-report.cron}", zone = "${app.timezone}")
    public void triggerDailyReportGeneration() {
        dailyReportUseCase.execute()
                .subscribe();
    }
}
