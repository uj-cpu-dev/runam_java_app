package rum_am_app.run_am.util;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import rum_am_app.run_am.exception.ApiException;
import rum_am_app.run_am.model.UserAd;
import rum_am_app.run_am.repository.UserAdRepository;

@Component
@RequiredArgsConstructor
public class AdValidator {

    private final UserAdRepository userAdRepository;

    public void validateAdCreation(UserAd userAd) {
        // If creating a draft, skip validation
        if (userAd.getStatus() == UserAd.AdStatus.DRAFT) {
            return;
        }

        // Default to ACTIVE if status is null
        UserAd.AdStatus status = userAd.getStatus() != null
                ? userAd.getStatus()
                : UserAd.AdStatus.ACTIVE;

        if (userAdRepository.existsByUserIdAndTitleAndPriceAndCategoryAndStatus(
                userAd.getUserId(),
                userAd.getTitle(),
                userAd.getPrice(),
                userAd.getCategory(),
                status
        )) {
            throw new ApiException(
                    "Duplicate active ad exists",
                    HttpStatus.BAD_REQUEST,
                    "DUPLICATE_AD"
            );
        }
    }
}
