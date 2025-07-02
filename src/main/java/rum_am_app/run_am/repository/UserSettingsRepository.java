package rum_am_app.run_am.repository;

import org.springframework.data.mongodb.repository.ExistsQuery;
import org.springframework.data.mongodb.repository.MongoRepository;
import rum_am_app.run_am.model.UserSettings;

import java.util.Optional;

public interface UserSettingsRepository extends MongoRepository<UserSettings, String> {
    Optional<UserSettings> findByUserId(String userId);
    @ExistsQuery("{ 'userId': ?0 }")
    boolean existsByUserId(String userId);
}
