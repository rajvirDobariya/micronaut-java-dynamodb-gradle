package micronaut.java.dynamodb.gradle.config;


import io.micronaut.context.annotation.Requires;
import io.micronaut.context.env.Environment;
import io.micronaut.context.event.ApplicationEventListener;
import io.micronaut.context.event.StartupEvent;
import jakarta.inject.Singleton;
import micronaut.java.dynamodb.gradle.model.Identified;
import micronaut.java.dynamodb.gradle.repo.DynamoRepository;
import micronaut.java.dynamodb.gradle.service.CustomerService;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.util.Arrays;

@Requires(property = "dynamodb.host")
@Requires(property = "dynamodb.port")
@Requires(env = Environment.DEVELOPMENT)
@Singleton
public class DevBootstrap implements ApplicationEventListener<StartupEvent> {

    private final DynamoRepository<? extends Identified> dynamoRepository;
    private final DynamoConfiguration dynamoConfiguration;
    private final CustomerService customerService;

    public DevBootstrap(DynamoRepository<? extends Identified> dynamoRepository, DynamoConfiguration dynamoConfiguration, CustomerService customerService) {
        this.dynamoRepository = dynamoRepository;
        this.dynamoConfiguration = dynamoConfiguration;
        this.customerService = customerService;
    }

    @Override
    public void onApplicationEvent(StartupEvent event) {

        if (!dynamoRepository.existsTable("Employee")) {

            CreateTableRequest createTableRequest = CreateTableRequest.builder()
                    .attributeDefinitions(AttributeDefinition.builder()
                            .attributeName("emp_id")
                            .attributeType(ScalarAttributeType.S)
                            .build())
                    .keySchema(KeySchemaElement.builder()
                            .attributeName("emp_id")
                            .keyType(KeyType.HASH)
                            .build())
                    .billingMode(BillingMode.PAY_PER_REQUEST)
                    .tableName("Employee")
                    .build();
            dynamoRepository.createTableV2(createTableRequest);
        }
        if (!dynamoRepository.existsTable("Department")) {

            CreateTableRequest createTableRequest = CreateTableRequest.builder()
                    .attributeDefinitions(AttributeDefinition.builder()
                            .attributeName("dept_id")
                            .attributeType(ScalarAttributeType.S)
                            .build())
                    .keySchema(KeySchemaElement.builder()
                            .attributeName("dept_id")
                            .keyType(KeyType.HASH)
                            .build())
                    .billingMode(BillingMode.PAY_PER_REQUEST)
                    .tableName("Department")
                    .build();
            dynamoRepository.createTableV2(createTableRequest);
        }

//        if (!dynamoRepository.existsTable("Customer")){
//            customerService.createCustomerTable();
//        }

    }


}