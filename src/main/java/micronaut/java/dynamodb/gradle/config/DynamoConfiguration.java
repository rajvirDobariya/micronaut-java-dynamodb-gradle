package micronaut.java.dynamodb.gradle.config;

import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.context.annotation.Requires;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

@Requires(property = "dynamodb.tables-name")
@ConfigurationProperties("dynamodb")
public interface DynamoConfiguration {
    @NotBlank
    List<String> getTablesName();
}