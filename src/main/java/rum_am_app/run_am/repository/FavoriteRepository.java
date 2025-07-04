package rum_am_app.run_am.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import rum_am_app.run_am.model.Favorite;

import java.util.List;
import java.util.Optional;

public interface FavoriteRepository extends MongoRepository<Favorite, String> {
    Optional<Favorite> findByUserIdAndAdId(String userId, String adId);
    List<Favorite> findByUserId(String userId);
    void deleteByUserIdAndAdId(String userId, String adId);
    List<Favorite> findByUserIdOrderByFavoritedAtDesc(String userId);

}
