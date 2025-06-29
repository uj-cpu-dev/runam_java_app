package rum_am_app.run_am.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import rum_am_app.run_am.model.UserAd;

import java.util.List;

public interface UserAdRepository extends MongoRepository<UserAd, String> {
    List<UserAd> findByUserIdAndStatus(String userId, UserAd.AdStatus status);
    List<UserAd> findByUserId(String userId);
    long countByUserIdAndStatus(String userId, UserAd.AdStatus status);
    long countByUserId(String userId);
    boolean existsByUserIdAndTitleAndPriceAndCategoryAndStatus(
            String userId,
            String title,
            double price,
            String category,
            UserAd.AdStatus adStatus
    );

    boolean existsByUserIdAndTitleAndPriceAndCategoryAndIdNot(
            String userId,
            String title,
            double price,
            String category,
            String id
    );
}
