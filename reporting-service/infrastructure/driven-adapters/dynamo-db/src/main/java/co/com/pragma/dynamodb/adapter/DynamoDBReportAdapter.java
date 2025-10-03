package co.com.pragma.dynamodb.adapter;

import co.com.pragma.dynamodb.entity.ReportEntity;
import co.com.pragma.dynamodb.helper.TemplateAdapterOperations;
import co.com.pragma.model.report.Report;
import co.com.pragma.model.report.gateways.ReportRepository;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.ReturnValue;
import software.amazon.awssdk.services.dynamodb.model.UpdateItemRequest;

import java.time.Instant;
import java.util.Map;


@Repository
public class DynamoDBReportAdapter extends TemplateAdapterOperations<
        Report,
        String,
        ReportEntity> implements ReportRepository {

    private final DynamoDbAsyncClient dynamoDbAsyncClient;
    private static final String TABLE_NAME = "reports";

    public DynamoDBReportAdapter(DynamoDbEnhancedAsyncClient connectionFactory, ObjectMapper mapper,
                                 DynamoDbAsyncClient dynamoDbAsyncClient) {
        /**
         *  Could be use mapper.mapBuilder if your domain model implement builder pattern
         *  super(repository, mapper, d -> mapper.mapBuilder(d,ObjectModel.ObjectModelBuilder.class).build());
         *  Or using mapper.map with the class of the object model
         */
        super(connectionFactory, mapper, d -> mapper.map(d, Report.class /*domain model*/), TABLE_NAME, new String[0] /*index is optional*/);
        this.dynamoDbAsyncClient = dynamoDbAsyncClient;
    }

    @Override
    public Mono<Report> findById(String id) {
        return super.getById(id);
    }

    public Mono<Report> incrementApprovedCount(String id, String state, Double amount) {

        Map<String, AttributeValue> key = Map.of(
                "id", AttributeValue.builder().s(id).build()
        );

        Map<String, AttributeValue> values = Map.of(
                ":inc", AttributeValue.builder().n("1").build(),
                ":amount", AttributeValue.builder().n(String.valueOf(amount)).build(),
                ":status", AttributeValue.builder().s(state).build(),
                ":updatedAt", AttributeValue.builder().s(Instant.now().toString()).build(),
                ":zero", AttributeValue.builder().n("0").build() // para if_not_exists
        );

        String updateExpression = "SET #status = :status, #updatedAt = :updatedAt, " +
                "#count = if_not_exists(#count, :zero) + :inc, " +
                "#totalAmount = if_not_exists(#totalAmount, :zero) + :amount";

        UpdateItemRequest request = UpdateItemRequest.builder()
                .tableName(TABLE_NAME)
                .key(key)
                .updateExpression(updateExpression)
                .expressionAttributeNames(Map.of(
                        "#count", "count",
                        "#totalAmount", "totalAmount",
                        "#status", "status",
                        "#updatedAt", "updatedAt"
                ))
                .expressionAttributeValues(values)
                .returnValues(ReturnValue.ALL_NEW)
                .build();

        return Mono.fromFuture(
                dynamoDbAsyncClient.updateItem(request)
        ).map(response -> {
            return new Report(
                    response.attributes().get("id").s(),
                    response.attributes().get("status").s(),
                    Long.parseLong(response.attributes().get("count").n()),
                    Double.parseDouble(response.attributes().get("totalAmount").n()),
                    Instant.parse(response.attributes().get("updatedAt").s())
            );
        });
    }
}
