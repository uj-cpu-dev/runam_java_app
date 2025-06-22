package rum_am_app.run_am.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import rum_am_app.run_am.model.User;

import java.util.Optional;

public interface UserRepository extends MongoRepository<User, String> {
    Optional<User> findByEmail(String email);
}
