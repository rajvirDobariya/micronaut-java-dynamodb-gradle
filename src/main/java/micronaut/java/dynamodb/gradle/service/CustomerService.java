package micronaut.java.dynamodb.gradle.service;

import jakarta.inject.Singleton;
import micronaut.java.dynamodb.gradle.model.Customer;
import software.amazon.awssdk.core.pagination.sync.SdkIterable;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.GetItemEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.PutItemEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.UpdateItemEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.DeleteItemEnhancedRequest;

import java.util.List;
import java.util.stream.Collectors;

@Singleton
public class CustomerService {

    private final DynamoDbEnhancedClient dynamoDbEnhancedClient;
    private final DynamoDbTable<Customer> customerTable;

    public CustomerService(DynamoDbEnhancedClient dynamoDbEnhancedClient) {
        this.dynamoDbEnhancedClient = dynamoDbEnhancedClient;
        this.customerTable = dynamoDbEnhancedClient.table("Customer", TableSchema.fromBean(Customer.class));
    }
    public List<Customer> findAll() {
        SdkIterable<Customer> items = customerTable.scan().items();
        List<Customer> collect = items.stream().collect(Collectors.toList());
        return collect;
    }

    public Customer createCustomer(Customer customer) {
        customerTable.putItem(customer);
        return customer;
    }
}
