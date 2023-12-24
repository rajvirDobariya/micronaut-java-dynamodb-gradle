package micronaut.java.dynamodb.gradle.service;


import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.util.CollectionUtils;
import jakarta.inject.Singleton;
import micronaut.java.dynamodb.gradle.config.DynamoConfiguration;
import micronaut.java.dynamodb.gradle.model.Book;
import micronaut.java.dynamodb.gradle.model.IdGenerator;
import micronaut.java.dynamodb.gradle.repo.BookRepository;
import micronaut.java.dynamodb.gradle.repo.DynamoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.PutItemResponse;
import software.amazon.awssdk.services.dynamodb.model.QueryRequest;
import software.amazon.awssdk.services.dynamodb.model.QueryResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Singleton
public class BookService extends DynamoRepository<Book> implements BookRepository {
    private static final Logger LOG = LoggerFactory.getLogger(BookService.class);
    private static final String ATTRIBUTE_ID = "id";
    private static final String ATTRIBUTE_ISBN = "isbn";
    private static final String ATTRIBUTE_NAME = "name";
    ObjectMapper objectMapper = new ObjectMapper();

    private final IdGenerator idGenerator;
    public BookService(DynamoDbClient dynamoDbClient,
                       DynamoConfiguration dynamoConfiguration,
                       IdGenerator idGenerator) {
        super(dynamoDbClient);
        this.idGenerator = idGenerator;
    }

    @Override
    @NonNull
    public String save(@NonNull @NotBlank String isbn,
                       @NonNull @NotBlank String name) throws JsonProcessingException {
        String id = idGenerator.generate();
        save(new Book(id, isbn, name));
        return id;
    }

    protected void save(@NonNull @NotNull @Valid Book book) throws JsonProcessingException {
        PutItemResponse itemResponse = dynamoDbClient.putItem(PutItemRequest.builder()
                .tableName(objectMapper.writeValueAsString(book))
                .item(item(book))
                .build());
        if (LOG.isDebugEnabled()) {
            LOG.debug(itemResponse.toString());
        }
    }

    @Override
    @NonNull
    public Optional<Book> findById(@NonNull @NotBlank String id) throws JsonProcessingException {
        return findById(Book.class, id)
                .map(this::bookOf);
    }

    @Override
    public void delete(@NonNull @NotBlank String id) throws JsonProcessingException {
        delete(Book.class, id);
    }

    @Override
    @NonNull
    public List<Book> findAll() throws JsonProcessingException {
        List<Book> result = new ArrayList<>();
        String beforeId = null;
        do {
            QueryRequest request = findAllQueryRequest(Book.class, beforeId, null);
            QueryResponse response = dynamoDbClient.query(request);
            if (LOG.isTraceEnabled()) {
                LOG.trace(response.toString());
            }
            result.addAll(parseInResponse(response));
            beforeId = lastEvaluatedId(response, Book.class).orElse(null);
        } while(beforeId != null);
        return result;
    }

    private List<Book> parseInResponse(QueryResponse response) {
        List<Map<String, AttributeValue>> items = response.items();
        List<Book> result = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(items)) {
            for (Map<String, AttributeValue> item : items) {
                result.add(bookOf(item));
            }
        }
        return result;
    }

    @NonNull
    private Book bookOf(@NonNull Map<String, AttributeValue> item) {
        return new Book(item.get(ATTRIBUTE_ID).s(),
                item.get(ATTRIBUTE_ISBN).s(),
                item.get(ATTRIBUTE_NAME).s());
    }

    @Override
    @NonNull
    protected Map<String, AttributeValue> item(@NonNull Book book) {
        Map<String, AttributeValue> result = super.item(book);
        result.put(ATTRIBUTE_ID, AttributeValue.builder().s(book.getId()).build());
        result.put(ATTRIBUTE_ISBN, AttributeValue.builder().s(book.getIsbn()).build());
        result.put(ATTRIBUTE_NAME, AttributeValue.builder().s(book.getName()).build());
        return result;
    }
}