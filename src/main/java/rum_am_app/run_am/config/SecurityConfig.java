package rum_am_app.run_am.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.boot.CommandLineRunner;

@Configuration
public class SecurityConfig {

    private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/health").permitAll()
                        .requestMatchers("/actuator/info").permitAll()
                        .requestMatchers("/api/public/**").permitAll()
                        .requestMatchers("/api/users/**").permitAll()
                        .anyRequest().authenticated()
                )
                .csrf(csrf -> csrf.disable())
                .headers(headers -> headers
                        .frameOptions().sameOrigin()
                        .httpStrictTransportSecurity().disable()
                );

        logger.info("Security configuration initialized");
        return http.build();
    }

    @Bean
    public CommandLineRunner mongoConnectionLogger(MongoTemplate mongoTemplate) {
        return args -> {
            try {
                String dbName = mongoTemplate.getDb().getName();
                    logger.info("✅ Successfully connected to MongoDB database: {}", dbName);
                logger.debug("Available collections: {}", mongoTemplate.getCollectionNames());

                // Test connection with a ping command
                mongoTemplate.executeCommand("{ ping: 1 }");
                logger.info("🗂️ MongoDB ping successful");
            } catch (Exception e) {
                logger.error("❌ MongoDB connection failed", e);
                throw e;
            }
        };
    }
}