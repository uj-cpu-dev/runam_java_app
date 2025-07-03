package rum_am_app.run_am.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
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
    @Query("{ 'status': 'ACTIVE' }")
    List<UserAd> findTop10ByStatusActiveOrderByDatePostedDesc(Pageable pageable);

    List<UserAd> findByStatus(String status, Pageable pageable);
}
