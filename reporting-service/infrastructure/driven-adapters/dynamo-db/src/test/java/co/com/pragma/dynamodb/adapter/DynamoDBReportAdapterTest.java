package co.com.pragma.dynamodb.adapter;

import co.com.pragma.dynamodb.entity.ReportEntity;
import co.com.pragma.model.report.Report;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.reactivecommons.utils.ObjectMapper;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import software.amazon.awssdk.enhanced.dynamodb.*;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class DynamoDBReportAdapterTest {

    @Mock
    private DynamoDbEnhancedAsyncClient enhancedClient;
    @Mock
    private DynamoDbAsyncTable<ReportEntity> table;
    @Mock
    private DynamoDbAsyncClient dynamoDbAsyncClient;
    @Mock
    private ObjectMapper mapper;

    private DynamoDBReportAdapter adapter;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(enhancedClient.table(any(), any(TableSchema.class))).thenReturn(table);
        adapter = new DynamoDBReportAdapter(enhancedClient, mapper, dynamoDbAsyncClient);
    }

    @Test
    void shouldSaveReport() {
        Report domain = new Report("1", "APPROVED", 10L, 200.0, Instant.now());
        ReportEntity entity = new ReportEntity("1", "APPROVED", 10L, 200.0, Instant.now());

        when(mapper.map(domain, ReportEntity.class)).thenReturn(entity);
        when(table.putItem(any(ReportEntity.class)))
                .thenReturn(CompletableFuture.completedFuture(null));

        Mono<Report> result = adapter.save(domain);

        StepVerifier.create(result)
                .expectNext(domain)
                .verifyComplete();

        verify(table).putItem(entity);
    }

    @Test
    void shouldGetById() {
        ReportEntity entity = new ReportEntity("1", "APPROVED", 5L, 100.0, Instant.now());
        Report domain = new Report("1", "APPROVED", 5L, 100.0, entity.getUpdatedAt());

        when(table.getItem(any(Key.class)))
                .thenReturn(CompletableFuture.completedFuture(entity));
        when(mapper.map(entity, Report.class)).thenReturn(domain);

        Mono<Report> result = adapter.findById("1");

        StepVerifier.create(result)
                .expectNext(domain)
                .verifyComplete();
    }

    @Test
    void shouldDeleteReport() {
        ReportEntity entity = new ReportEntity("1", "PENDING", 0L, 0.0, Instant.now());
        Report domain = new Report("1", "PENDING", 0L, 0.0, entity.getUpdatedAt());

        when(mapper.map(domain, ReportEntity.class)).thenReturn(entity);
        when(table.deleteItem(any(ReportEntity.class)))
                .thenReturn(CompletableFuture.completedFuture(entity));
        when(mapper.map(entity, Report.class)).thenReturn(domain);

        Mono<Report> result = adapter.delete(domain);

        StepVerifier.create(result)
                .expectNext(domain)
                .verifyComplete();
    }

    @Test
    void shouldIncrementApprovedCount() {
        String id = "123";
        Instant now = Instant.now();

        Map<String, AttributeValue> attributes = Map.of(
                "id", AttributeValue.builder().s(id).build(),
                "status", AttributeValue.builder().s("APPROVED").build(),
                "count", AttributeValue.builder().n("1").build(),
                "totalAmount", AttributeValue.builder().n("100.0").build(),
                "updatedAt", AttributeValue.builder().s(now.toString()).build()
        );

        UpdateItemResponse response = UpdateItemResponse.builder()
                .attributes(attributes)
                .build();

        when(dynamoDbAsyncClient.updateItem(any(UpdateItemRequest.class)))
                .thenReturn(CompletableFuture.completedFuture(response));

        Mono<Report> result = adapter.incrementApprovedCount(id, "APPROVED", 100.0);

        StepVerifier.create(result)
                .expectNextMatches(r -> r.getId().equals(id)
                        && r.getStatus().equals("APPROVED")
                        && r.getCount() == 1L
                        && r.getTotalAmount() == 100.0)
                .verifyComplete();
    }
}