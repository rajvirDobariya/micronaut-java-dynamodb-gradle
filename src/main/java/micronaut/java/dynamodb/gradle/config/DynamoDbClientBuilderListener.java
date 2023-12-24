package micronaut.java.dynamodb.gradle.config;

import io.micronaut.context.annotation.Requires;
import io.micronaut.context.annotation.Value;
import io.micronaut.context.event.BeanCreatedEvent;
import io.micronaut.context.event.BeanCreatedEventListener;
import io.micronaut.context.exceptions.ConfigurationException;
import jakarta.inject.Singleton;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClientBuilder;

import java.net.URI;
import java.net.URISyntaxException;

@Requires(property = "dynamodb.host")
@Requires(property = "dynamodb.port")
@Requires(property = "aws.region") // Ensure AWS region is set
@Singleton
class DynamoDbClientBuilderListener implements BeanCreatedEventListener<DynamoDbClientBuilder> {

    // 1 declare variables
    private final URI endpoint;
    private final String accessKeyId;
    private final String secretAccessKey;
    private final Region region; // Add region field

    // 2 Assign variables values
    DynamoDbClientBuilderListener(@Value("${dynamodb.host}") String host,
                                  @Value("${dynamodb.port}") String port,
                                  @Value("${aws.region}") String awsRegion) {
        try {
            this.endpoint = new URI("http://" + host + ":" + port);
        } catch (URISyntaxException e) {
            throw new ConfigurationException("dynamodb.endpoint not a valid URI");
        }
        this.accessKeyId = "fakeMyKeyId";
        this.secretAccessKey = "fakeSecretAccessKey";
        this.region = Region.of(awsRegion); // Initialize region
    }

    //3 Create  DynamoDB Connection Bean
    @Override
    public DynamoDbClientBuilder onCreated(BeanCreatedEvent<DynamoDbClientBuilder> event) {
        return event.getBean()
                .region(region) // Set the AWS region
                .endpointOverride(endpoint)
                .credentialsProvider(() -> new AwsCredentials() {
                    @Override
                    public String accessKeyId() {
                        return accessKeyId;
                    }

                    @Override
                    public String secretAccessKey() {
                        return secretAccessKey;
                    }
                });
    }
}
