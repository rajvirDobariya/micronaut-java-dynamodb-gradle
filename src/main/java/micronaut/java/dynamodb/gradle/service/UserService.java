package micronaut.java.dynamodb.gradle.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.util.CollectionUtils;
import jakarta.inject.Singleton;
import micronaut.java.dynamodb.gradle.request.UserRequest;
import micronaut.java.dynamodb.gradle.config.DynamoConfiguration;
import micronaut.java.dynamodb.gradle.model.User;
import micronaut.java.dynamodb.gradle.model.IdGenerator;
import micronaut.java.dynamodb.gradle.repo.UserRepository;
import micronaut.java.dynamodb.gradle.repo.DynamoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.PutItemResponse;
import software.amazon.awssdk.services.dynamodb.model.QueryRequest;
import software.amazon.awssdk.services.dynamodb.model.QueryResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Singleton
public class UserService extends DynamoRepository<User> implements UserRepository {
    private static final Logger LOG = LoggerFactory.getLogger(UserService.class);
    private static final String ATTRIBUTE_ID = "id";
    private static final String ATTRIBUTE_EMAIL = "email";
    private static final String ATTRIBUTE_PASSWORD = "password";
    private static final String ATTRIBUTE_FIRST_NAME = "firstName";
    private static final String ATTRIBUTE_LAST_NAME = "lastName";
    private static final String ATTRIBUTE_COUNTRY_CODE = "countryCode";
    private static final String ATTRIBUTE_PHONE_NO = "phoneNo";

    private final IdGenerator idGenerator;

    public UserService(DynamoDbClient dynamoDbClient,
                       DynamoConfiguration dynamoConfiguration,
                       IdGenerator idGenerator) {
        super(dynamoDbClient);
        this.idGenerator = idGenerator;
    }

    @Override
    public String save(UserRequest userRequest) throws JsonProcessingException {
        String id = idGenerator.generate();
        User user = userRequest.getUser(userRequest);
        user.setId(id);
        PutItemResponse itemResponse = dynamoDbClient.putItem(PutItemRequest.builder()
                .tableName("user") // Use class name as table name for User
                .item(item(user))
                .build());
        if (LOG.isDebugEnabled()) {
            LOG.debug(itemResponse.toString());
        }
        return user.getId();
    }

    @Override
    @NonNull
    public Optional<User> findById(@NonNull String id) throws JsonProcessingException {
        return findById(User.class, id)
                .map(this::userOf);
    }

    @Override
    public void delete(@NonNull String id) throws JsonProcessingException {
        delete(User.class, id);
    }


    @Override
    @NonNull
    public List<User> findAll() throws JsonProcessingException {
        List<User> result = new ArrayList<>();
        String beforeId = null;
        do {
            QueryRequest request = findAllQueryRequest(User.class, beforeId, null);
            QueryResponse response = dynamoDbClient.query(request);
            if (LOG.isTraceEnabled()) {
                LOG.trace(response.toString());
            }
            result.addAll(parseInResponse(response));
            beforeId = lastEvaluatedId(response, User.class).orElse(null);
        } while(beforeId != null);
        return result;
    }

    private List<User> parseInResponse(QueryResponse response) {
        List<Map<String, AttributeValue>> items = response.items();
        List<User> result = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(items)) {
            for (Map<String, AttributeValue> item : items) {
                result.add(userOf(item));
            }
        }
        return result;
    }

    @NonNull
    private User userOf(@NonNull Map<String, AttributeValue> item) {
        String id = getStringAttributeValue(item, ATTRIBUTE_ID);
        String email = getStringAttributeValue(item, ATTRIBUTE_EMAIL);
        String password = getStringAttributeValue(item, ATTRIBUTE_PASSWORD);
        String firstName = getStringAttributeValue(item, ATTRIBUTE_FIRST_NAME);
        String lastName = getStringAttributeValue(item, ATTRIBUTE_LAST_NAME);
        String countryCode = getStringAttributeValue(item, ATTRIBUTE_COUNTRY_CODE);
        String phoneNo = getStringAttributeValue(item, ATTRIBUTE_PHONE_NO);

        return new User(id, email, password, firstName, lastName, countryCode, phoneNo);
    }

    @Nullable
    private String getStringAttributeValue(@NonNull Map<String, AttributeValue> item, @NonNull String attributeName) {
        AttributeValue attributeValue = item.get(attributeName);
        return (attributeValue != null && attributeValue.s() != null) ? attributeValue.s() : null;
    }
    @Override
    @NonNull
    protected Map<String, AttributeValue> item(@NonNull User user) {
        Map<String, AttributeValue> result = super.item(user);
        result.put(ATTRIBUTE_ID, AttributeValue.builder().s(user.getId()).build());
        result.put(ATTRIBUTE_EMAIL, AttributeValue.builder().s(user.getEmail()).build());
        result.put(ATTRIBUTE_PASSWORD, AttributeValue.builder().s(user.getPassword()).build());
        return result;
    }
}
