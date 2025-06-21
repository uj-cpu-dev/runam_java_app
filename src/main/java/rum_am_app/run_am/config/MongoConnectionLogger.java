package rum_am_app.run_am.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;

@Configuration
public class MongoConnectionLogger {

    private static final Logger logger = LoggerFactory.getLogger(MongoConnectionLogger.class);

    @Bean
    public CommandLineRunner logMongoConnection(MongoTemplate mongoTemplate) {
        return args -> {
            try {
                String dbName = mongoTemplate.getDb().getName();
                logger.info("✅ Connected to MongoDB successfully");
                logger.info("📊 Database name: {}", dbName);
                logger.info("📦 Collections: {}", mongoTemplate.getCollectionNames());
            } catch (Exception e) {
                logger.error("❌ Failed to connect to MongoDB", e);
                throw e;
            }
        };
    }
}
