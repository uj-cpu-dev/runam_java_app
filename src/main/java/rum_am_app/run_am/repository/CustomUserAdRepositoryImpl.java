package rum_am_app.run_am.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;
import rum_am_app.run_am.dtorequest.AdFilterRequest;
import rum_am_app.run_am.model.UserAd;

import java.util.List;
import java.util.regex.Pattern;

@Repository
@RequiredArgsConstructor
class CustomUserAdRepositoryImpl implements CustomUserAdRepository {

    private final MongoTemplate mongoTemplate;

    @Override
    public Page<UserAd> findFilteredAds(AdFilterRequest filter, Pageable pageable) {
        Criteria criteria = new Criteria();

        if (filter.getStatus() != null) {
            criteria.and("status").is(filter.getStatus());
        }

        if (filter.getCategory() != null && !filter.getCategory().isEmpty()) {
            criteria.and("category").regex(".*" + Pattern.quote(filter.getCategory()) + ".*", "i");
        }

        if (filter.getLocation() != null && !filter.getLocation().isEmpty()) {
            criteria.and("location").regex(".*" + Pattern.quote(filter.getLocation()) + ".*", "i");
        }

        if (filter.getCondition() != null && !filter.getCondition().isEmpty()) {
            criteria.and("condition").regex(".*" + Pattern.quote(filter.getCondition()) + ".*", "i");
        }

        if (filter.getMinPrice() != null) {
            criteria.and("price").gte(filter.getMinPrice());
        }

        if (filter.getMaxPrice() != null) {
            criteria.and("price").lte(filter.getMaxPrice());
        }

        if (filter.getPostedAfter() != null) {
            criteria.and("datePosted").gte(filter.getPostedAfter());
        }

        if (filter.getSearchQuery() != null && !filter.getSearchQuery().isEmpty()) {
            criteria.orOperator(
                    Criteria.where("title").regex(Pattern.quote(filter.getSearchQuery()), "i"),
                    Criteria.where("description").regex(Pattern.quote(filter.getSearchQuery()), "i")
            );
        }

        Query query = new Query(criteria).with(pageable);
        long count = mongoTemplate.count(query, UserAd.class);
        List<UserAd> ads = mongoTemplate.find(query, UserAd.class);

        return new PageImpl<>(ads, pageable, count);
    }

}