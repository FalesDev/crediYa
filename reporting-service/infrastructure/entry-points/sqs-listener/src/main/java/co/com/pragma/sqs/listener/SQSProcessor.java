package co.com.pragma.sqs.listener;

import co.com.pragma.model.report.ReportApprovedCountMessage;
import co.com.pragma.usecase.updateapprovedcount.UpdateApprovedCountUseCase;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.services.sqs.model.Message;

import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class SQSProcessor implements Function<Message, Mono<Void>> {
    private final UpdateApprovedCountUseCase updateApprovedCountUseCase;
    private final ObjectMapper mapper;

    @Override
    public Mono<Void> apply(Message message) {
        try {
            ReportApprovedCountMessage reportApprovedCountMessage =
                    mapper.readValue(message.body(), ReportApprovedCountMessage.class);

            return updateApprovedCountUseCase.incrementApprovedRequestsCount(reportApprovedCountMessage);
        } catch (Exception e) {
            return Mono.error(e);
        }
    }
}
