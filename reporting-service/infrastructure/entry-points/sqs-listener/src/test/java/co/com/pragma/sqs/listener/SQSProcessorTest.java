package co.com.pragma.sqs.listener;

import co.com.pragma.model.report.ReportApprovedCountMessage;
import co.com.pragma.usecase.updateapprovedcount.UpdateApprovedCountUseCase;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import software.amazon.awssdk.services.sqs.model.Message;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SQSProcessorTest {

    @Mock
    private UpdateApprovedCountUseCase updateApprovedCountUseCase;
    private final ObjectMapper mapper = new ObjectMapper();

    private SQSProcessor sqsProcessor;

    @BeforeEach
    void setUp() {
        sqsProcessor = new SQSProcessor(updateApprovedCountUseCase, mapper);
    }

    @Test
    void apply_whenMessageIsValid_shouldProcessSuccessfully() throws JsonProcessingException {
        ReportApprovedCountMessage reportMessage = ReportApprovedCountMessage.builder()
                .applicationId(UUID.randomUUID())
                .amount(15000.50)
                .state("APPROVED")
                .build();

        String messageBody = mapper.writeValueAsString(reportMessage);
        Message sqsMessage = Message.builder().body(messageBody).build();

        when(updateApprovedCountUseCase.incrementApprovedRequestsCount(any(ReportApprovedCountMessage.class)))
                .thenReturn(Mono.empty());

        Mono<Void> result = sqsProcessor.apply(sqsMessage);

        StepVerifier.create(result)
                .verifyComplete();
        verify(updateApprovedCountUseCase).incrementApprovedRequestsCount(any(ReportApprovedCountMessage.class));
    }

    @Test
    void apply_whenMessageBodyIsInvalid_shouldReturnMonoError() {
        String invalidJsonBody = "{\"applicationId\":\"" + UUID.randomUUID() + "\", \"amount\":15000.50"; // Falta '}'
        Message sqsMessage = Message.builder().body(invalidJsonBody).build();

        Mono<Void> result = sqsProcessor.apply(sqsMessage);

        StepVerifier.create(result)
                .expectError(JsonProcessingException.class)
                .verify();

        verify(updateApprovedCountUseCase, never()).incrementApprovedRequestsCount(any());
    }

    @Test
    void apply_whenUseCaseFails_shouldPropagateError() throws JsonProcessingException {
        ReportApprovedCountMessage reportMessage = ReportApprovedCountMessage.builder()
                .applicationId(UUID.randomUUID())
                .amount(2000.0)
                .state("REJECTED")
                .build();

        String messageBody = mapper.writeValueAsString(reportMessage);
        Message sqsMessage = Message.builder().body(messageBody).build();

        RuntimeException expectedException = new RuntimeException("Error connecting to database!");
        when(updateApprovedCountUseCase.incrementApprovedRequestsCount(any(ReportApprovedCountMessage.class)))
                .thenReturn(Mono.error(expectedException));

        Mono<Void> result = sqsProcessor.apply(sqsMessage);

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof RuntimeException &&
                        throwable.getMessage().equals("Error connecting to database!"))
                .verify();

        verify(updateApprovedCountUseCase).incrementApprovedRequestsCount(any(ReportApprovedCountMessage.class));
    }
}