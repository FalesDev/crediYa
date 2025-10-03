package co.com.pragma.sqs.sender.adapter;

import co.com.pragma.model.dailyreport.DailyReport;
import co.com.pragma.sqs.sender.SQSSender;
import co.com.pragma.sqs.sender.factory.SqsMessageFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import software.amazon.awssdk.services.sqs.model.MessageAttributeValue;

import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DailyReportAdapterTest {

    @Mock
    private SQSSender sqsSender;

    @Mock
    private SqsMessageFactory messageFactory;

    private DailyReportAdapter adapter;

    private final String notificationQueue = "test-queue";

    @BeforeEach
    void setUp() {
        adapter = new DailyReportAdapter(sqsSender, messageFactory, notificationQueue);
    }

    @Test
    void sendDailyReport_ShouldSendMessageWithCorrectParameters() {
        DailyReport dailyReport = DailyReport.builder()
                .approvedLoansCount(10L)
                .totalLoanAmount(50000.0)
                .build();

        String expectedJson = "{\"approvedLoansCount\":10,\"totalLoanAmount\":50000.0}";
        Map<String, MessageAttributeValue> expectedAttributes = Map.of(
                "eventType", MessageAttributeValue.builder()
                        .dataType("String")
                        .stringValue("DAILY_APPROVED_APPLICATION_REPORT")
                        .build()
        );

        when(messageFactory.toJson(dailyReport)).thenReturn(expectedJson);
        when(messageFactory.buildAttributes(any())).thenReturn(expectedAttributes);
        when(sqsSender.send(anyString(), anyString(), any())).thenReturn(Mono.empty());

        Mono<Void> result = adapter.sendDailyReport(dailyReport);

        StepVerifier.create(result)
                .verifyComplete();

        verify(messageFactory).toJson(dailyReport);
        verify(messageFactory).buildAttributes(Map.of(
                "eventType", "DAILY_APPROVED_APPLICATION_REPORT"
        ));
        verify(sqsSender).send(notificationQueue, expectedJson, expectedAttributes);
    }

    @Test
    void sendDailyReport_WhenSqsSenderFails_ShouldPropagateError() {
        DailyReport dailyReport = DailyReport.builder()
                .approvedLoansCount(5L)
                .totalLoanAmount(25000.0)
                .build();

        String expectedJson = "{\"approvedLoansCount\":5,\"totalLoanAmount\":25000.0}";
        Map<String, MessageAttributeValue> expectedAttributes = Map.of(
                "eventType", MessageAttributeValue.builder()
                        .dataType("String")
                        .stringValue("DAILY_APPROVED_APPLICATION_REPORT")
                        .build()
        );

        RuntimeException expectedException = new RuntimeException("SQS error");

        when(messageFactory.toJson(dailyReport)).thenReturn(expectedJson);
        when(messageFactory.buildAttributes(any())).thenReturn(expectedAttributes);
        when(sqsSender.send(anyString(), anyString(), any())).thenReturn(Mono.error(expectedException));

        StepVerifier.create(adapter.sendDailyReport(dailyReport))
                .expectError(RuntimeException.class)
                .verify();

        verify(sqsSender).send(notificationQueue, expectedJson, expectedAttributes);
    }

    @Test
    void sendDailyReport_ShouldUseCorrectEventTypeAttribute() {
        DailyReport dailyReport = DailyReport.builder()
                .approvedLoansCount(1L)
                .totalLoanAmount(1000.0)
                .build();

        String expectedJson = "json-content";
        Map<String, MessageAttributeValue> expectedAttributes = Map.of(
                "eventType", MessageAttributeValue.builder()
                        .dataType("String")
                        .stringValue("DAILY_APPROVED_APPLICATION_REPORT")
                        .build()
        );

        when(messageFactory.toJson(dailyReport)).thenReturn(expectedJson);
        when(messageFactory.buildAttributes(any())).thenReturn(expectedAttributes);
        when(sqsSender.send(anyString(), anyString(), any())).thenReturn(Mono.empty());

        adapter.sendDailyReport(dailyReport).block();

        verify(messageFactory).buildAttributes(Map.of(
                "eventType", "DAILY_APPROVED_APPLICATION_REPORT"
        ));
    }
}
