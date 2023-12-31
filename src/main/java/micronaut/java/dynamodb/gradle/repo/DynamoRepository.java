package micronaut.java.dynamodb.gradle.repo;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micronaut.context.annotation.Primary;
import io.micronaut.context.annotation.Requires;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.util.CollectionUtils;
import jakarta.inject.Singleton;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import micronaut.java.dynamodb.gradle.config.DynamoConfiguration;
import micronaut.java.dynamodb.gradle.model.Identified;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.util.*;

//@Requires(condition = CIAwsRegionProviderChainCondition.class)
//@Requires(condition = CIAwsCredentialsProviderChainCondition.class)
@Requires(beans = {DynamoConfiguration.class, DynamoDbClient.class})
@Singleton
@Primary
public class DynamoRepository<T extends Identified> {
    private static final Logger LOG = LoggerFactory.getLogger(DynamoRepository.class);
    protected static final String HASH = "#";
    protected static final String ATTRIBUTE_PK = "pk";
    protected static final String ATTRIBUTE_SK = "sk";
    protected static final String ATTRIBUTE_GSI_1_PK = "GSI1PK";
    protected static final String ATTRIBUTE_GSI_1_SK = "GSI1SK";
    protected static final String INDEX_GSI_1 = "GSI1";

    protected final DynamoDbClient dynamoDbClient;
    ObjectMapper objectMapper = new ObjectMapper();

    public DynamoRepository(DynamoDbClient dynamoDbClient) {
        this.dynamoDbClient = dynamoDbClient;
    }

    public boolean existsTable(String tableName) {
        try {
            dynamoDbClient.describeTable(DescribeTableRequest.builder()
                    .tableName(tableName)
                    .build());
            return true;
        } catch (ResourceNotFoundException e) {
            return false;
        }
    }


    public void createTable(String tableName) {
        dynamoDbClient.createTable(CreateTableRequest.builder()
                .attributeDefinitions(AttributeDefinition.builder()
                                .attributeName(ATTRIBUTE_PK)
                                .attributeType(ScalarAttributeType.S)
                                .build(),
                        AttributeDefinition.builder()
                                .attributeName(ATTRIBUTE_SK)
                                .attributeType(ScalarAttributeType.S)
                                .build(),
                        AttributeDefinition.builder()
                                .attributeName(ATTRIBUTE_GSI_1_PK)
                                .attributeType(ScalarAttributeType.S)
                                .build(),
                        AttributeDefinition.builder()
                                .attributeName(ATTRIBUTE_GSI_1_SK)
                                .attributeType(ScalarAttributeType.S)
                                .build())
                .keySchema(Arrays.asList(KeySchemaElement.builder()
                                .attributeName(ATTRIBUTE_PK)
                                .keyType(KeyType.HASH)
                                .build(),
                        KeySchemaElement.builder()
                                .attributeName(ATTRIBUTE_SK)
                                .keyType(KeyType.RANGE)
                                .build()))
                .billingMode(BillingMode.PAY_PER_REQUEST)
                .tableName(tableName)
                .globalSecondaryIndexes(gsi1())
                .build());
    }

    public void createTableV2(CreateTableRequest cliCreateTableRequest) {
            dynamoDbClient.createTable(cliCreateTableRequest);
    }

    @NonNull
    public QueryRequest findAllQueryRequest(@NonNull Class<?> cls,
                                            @Nullable String beforeId,
                                            @Nullable Integer limit) throws JsonProcessingException {
        QueryRequest.Builder builder = QueryRequest.builder()
                .tableName(cls.getSimpleName().toLowerCase())
                .indexName(INDEX_GSI_1)
                .scanIndexForward(false);
        if (limit != null) {
            builder.limit(limit);
        }
        if (beforeId == null) {
            return builder.keyConditionExpression("#pk = :pk")
                    .expressionAttributeNames(Collections.singletonMap("#pk", ATTRIBUTE_GSI_1_PK))
                    .expressionAttributeValues(Collections.singletonMap(":pk",
                            classAttributeValue(cls)))
                    .build();
        } else {
            return builder.keyConditionExpression("#pk = :pk and #sk < :sk")
                    .expressionAttributeNames(CollectionUtils.mapOf("#pk", ATTRIBUTE_GSI_1_PK, "#sk", ATTRIBUTE_GSI_1_SK))
                    .expressionAttributeValues(CollectionUtils.mapOf(":pk",
                            classAttributeValue(cls),
                            ":sk",
                            id(cls, beforeId)
                    ))
                    .build();
        }
    }

    protected void delete(@NonNull @NotNull Class<?> cls, @NonNull @NotBlank String id) throws JsonProcessingException {
        AttributeValue pk = id(cls, id);
        DeleteItemResponse deleteItemResponse = dynamoDbClient.deleteItem(DeleteItemRequest.builder()
                .tableName(cls.getSimpleName().toLowerCase())
                .key(CollectionUtils.mapOf(ATTRIBUTE_PK, pk, ATTRIBUTE_SK, pk))
                .build());
        if (LOG.isDebugEnabled()) {
            LOG.debug(deleteItemResponse.toString());
        }
    }

    protected Optional<Map<String, AttributeValue>> findById(@NonNull @NotNull Class<?> cls, @NonNull @NotBlank String id) throws JsonProcessingException {
        AttributeValue pk = id(cls, id);
        GetItemResponse getItemResponse = dynamoDbClient.getItem(GetItemRequest.builder()
                .tableName(cls.getSimpleName().toLowerCase())
                .key(CollectionUtils.mapOf(ATTRIBUTE_PK, pk, ATTRIBUTE_SK, pk))
                .build());
        return !getItemResponse.hasItem() ? Optional.empty() : Optional.of(getItemResponse.item());
    }

    @NonNull
    public static Optional<String> lastEvaluatedId(@NonNull QueryResponse response,
                                                   @NonNull Class<?> cls) {
        if (response.hasLastEvaluatedKey()) {
            Map<String, AttributeValue> item = response.lastEvaluatedKey();
            if (item != null && item.containsKey(ATTRIBUTE_PK)) {
                return id(cls, item.get(ATTRIBUTE_PK));
            }
        }
        return Optional.empty();
    }

    private static GlobalSecondaryIndex gsi1() {
        return GlobalSecondaryIndex.builder()
                .indexName(INDEX_GSI_1)
                .keySchema(KeySchemaElement.builder()
                        .attributeName(ATTRIBUTE_GSI_1_PK)
                        .keyType(KeyType.HASH)
                        .build(), KeySchemaElement.builder()
                        .attributeName(ATTRIBUTE_GSI_1_SK)
                        .keyType(KeyType.RANGE)
                        .build())
                .projection(Projection.builder()
                        .projectionType(ProjectionType.ALL)
                        .build())
                .build();
    }

    @NonNull
    protected Map<String, AttributeValue> item(@NonNull T entity) {
        Map<String, AttributeValue> item = new HashMap<>();
        AttributeValue pk = id(entity.getClass(), entity.getId());
        item.put(ATTRIBUTE_PK, pk);
        item.put(ATTRIBUTE_SK, pk);
        item.put(ATTRIBUTE_GSI_1_PK, classAttributeValue(entity.getClass()));
        item.put(ATTRIBUTE_GSI_1_SK, pk);
        return item;
    }

    @NonNull
    protected static AttributeValue classAttributeValue(@NonNull Class<?> cls) {
        return AttributeValue.builder()
                .s(cls.getSimpleName())
                .build();
    }

    @NonNull
    protected static AttributeValue id(@NonNull Class<?> cls,
                                       @NonNull String id) {
        return AttributeValue.builder()
                .s(String.join(HASH, cls.getSimpleName().toUpperCase(), id))
                .build();
    }

    @NonNull
    protected static Optional<String> id(@NonNull Class<?> cls,
                                         @NonNull AttributeValue attributeValue) {
        String str = attributeValue.s();
        String substring = cls.getSimpleName().toUpperCase() + HASH;
        return str.startsWith(substring) ? Optional.of(str.substring(substring.length())) : Optional.empty();
    }
}