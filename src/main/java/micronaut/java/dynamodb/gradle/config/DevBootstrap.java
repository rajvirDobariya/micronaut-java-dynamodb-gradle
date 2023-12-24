package micronaut.java.dynamodb.gradle.config;


import io.micronaut.context.annotation.Requires;
import io.micronaut.context.env.Environment;
import io.micronaut.context.event.ApplicationEventListener;
import io.micronaut.context.event.StartupEvent;
import jakarta.inject.Singleton;
import micronaut.java.dynamodb.gradle.model.Identified;
import micronaut.java.dynamodb.gradle.repo.DynamoRepository;

import java.util.List;

@Requires(property = "dynamodb.host")
@Requires(property = "dynamodb.port")
@Requires(env = Environment.DEVELOPMENT)
@Singleton
public class DevBootstrap implements ApplicationEventListener<StartupEvent> {

    private final DynamoRepository<? extends Identified> dynamoRepository;
    private final DynamoConfiguration dynamoConfiguration;

    public DevBootstrap(DynamoRepository<? extends Identified> dynamoRepository, DynamoConfiguration dynamoConfiguration) {
        this.dynamoRepository = dynamoRepository;
        this.dynamoConfiguration = dynamoConfiguration;
    }

    @Override
    public void onApplicationEvent(StartupEvent event) {
        List<String> tableNames = dynamoConfiguration.getTablesName();

        for (String tableName : tableNames) {
            if (!dynamoRepository.existsTable(tableName)) {
                dynamoRepository.createTable(tableName);
            }
        }
    }
}