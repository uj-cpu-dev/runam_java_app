package rum_am_app.run_am.config;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

@Configuration
public class DynamoDbConfig {
    private static final Logger logger = LoggerFactory.getLogger(DynamoDbConfig.class);

    @Bean
    public DynamoDbClient dynamoDbClient() {
        return DynamoDbClient.builder()
                .region(Region.US_EAST_1)
                .build();
    }

    @PostConstruct
    public void testDynamoConnection() {
        try (DynamoDbClient client = dynamoDbClient()) {
            client.listTables(); // Lightweight operation
            logger.info("✅ Successfully connected to DynamoDB.");
        } catch (Exception e) {
            logger.error("❌ Failed to connect to DynamoDB", e);
        }
    }
}
