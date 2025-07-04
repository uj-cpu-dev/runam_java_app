package rum_am_app.run_am.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import rum_am_app.run_am.dtorequest.AdFilterRequest;
import rum_am_app.run_am.model.UserAd;

public interface CustomUserAdRepository {
    Page<UserAd> findFilteredAds(AdFilterRequest filter, Pageable pageable);
}
