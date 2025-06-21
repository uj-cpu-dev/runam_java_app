package rum_am_app.run_am.config;

import com.mongodb.client.MongoClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MongoConnectionLogger {

    private static final Logger logger = LoggerFactory.getLogger(MongoConnectionLogger.class);

    @Bean
    public CommandLineRunner logMongoConnection(MongoClient mongoClient) {
        return args -> {
            try {
                mongoClient.listDatabaseNames().first();
                logger.info("✅ Connected to MongoDB successfully");
            } catch (Exception e) {
                logger.error("❌ Failed to connect to MongoDB", e);
            }
        };
    }
}
