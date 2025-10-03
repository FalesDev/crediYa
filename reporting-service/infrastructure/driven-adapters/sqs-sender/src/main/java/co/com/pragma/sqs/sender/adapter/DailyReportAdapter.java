package co.com.pragma.sqs.sender.adapter;

import co.com.pragma.model.dailyreport.DailyReport;
import co.com.pragma.model.dailyreport.gateways.DailyReportGateway;
import co.com.pragma.sqs.sender.SQSSender;
import co.com.pragma.sqs.sender.factory.SqsMessageFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Map;

@Component
public class DailyReportAdapter implements DailyReportGateway {

    private final SQSSender sqsSender;
    private final SqsMessageFactory messageFactory;
    private final String notificationQueue;

    public DailyReportAdapter(
            SQSSender sqsSender,
            SqsMessageFactory messageFactory,
            @Value("${queue.names.notifications}") String notificationQueue
    ) {
        this.sqsSender = sqsSender;
        this.messageFactory = messageFactory;
        this.notificationQueue = notificationQueue;
    }

    @Override
    public Mono<Void> sendDailyReport(DailyReport payload) {
        var attributes = Map.of(
                "eventType", "DAILY_APPROVED_APPLICATION_REPORT"
        );

        return sqsSender.send(
                notificationQueue,
                messageFactory.toJson(payload),
                messageFactory.buildAttributes(attributes)
        ).then();
    }
}
