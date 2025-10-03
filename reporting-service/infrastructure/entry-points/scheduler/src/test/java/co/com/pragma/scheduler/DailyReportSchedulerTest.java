package co.com.pragma.scheduler;

import co.com.pragma.usecase.dailyreport.DailyReportUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DailyReportSchedulerTest {

    @Mock
    private DailyReportUseCase dailyReportUseCase;

    private DailyReportScheduler scheduler;

    @BeforeEach
    void setUp() {
        scheduler = new DailyReportScheduler(dailyReportUseCase);
    }

    @Test
    void triggerDailyReportGeneration_ShouldCallUseCase() {
        when(dailyReportUseCase.execute()).thenReturn(Mono.empty());

        scheduler.triggerDailyReportGeneration();

        verify(dailyReportUseCase, times(1)).execute();
    }

    @Test
    void triggerDailyReportGeneration_ShouldSubscribeToMono() {
        when(dailyReportUseCase.execute()).thenReturn(Mono.empty());

        scheduler.triggerDailyReportGeneration();

        verify(dailyReportUseCase, times(1)).execute();
    }

    @Test
    void triggerDailyReportGeneration_ShouldHandleUseCaseSuccess() {
        when(dailyReportUseCase.execute()).thenReturn(Mono.empty());

        assertDoesNotThrow(() -> {
            scheduler.triggerDailyReportGeneration();
        });

        verify(dailyReportUseCase, times(1)).execute();
    }

    @Test
    void triggerDailyReportGeneration_ShouldHandleUseCaseError() {
        RuntimeException expectedException = new RuntimeException("Error generating report");
        when(dailyReportUseCase.execute()).thenReturn(Mono.error(expectedException));

        assertDoesNotThrow(() -> {
            scheduler.triggerDailyReportGeneration();
        });

        verify(dailyReportUseCase, times(1)).execute();
    }
}
