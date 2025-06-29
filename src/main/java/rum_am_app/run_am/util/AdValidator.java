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
        if (userAdRepository.existsByUserIdAndTitleAndPriceAndCategoryAndStatus(
                userAd.getUserId(),
                userAd.getTitle(),
                userAd.getPrice(),
                userAd.getCategory(),
                UserAd.AdStatus.ACTIVE
        )) {
            throw new ApiException(
                    "Duplicate active ad exists",
                    HttpStatus.BAD_REQUEST,
                    "DUPLICATE_AD"
            );
        }
    }

    public void validateAdUpdate(UserAd userAd, String excludeId) {
        if (userAdRepository.existsByUserIdAndTitleAndPriceAndCategoryAndIdNot(
                userAd.getUserId(),
                userAd.getTitle(),
                userAd.getPrice(),
                userAd.getCategory(),
                excludeId
        )) {
            throw new ApiException(
                    "Duplicate active ad exists",
                    HttpStatus.BAD_REQUEST,
                    "DUPLICATE_AD"
            );
        }
    }

    public UserAd validateAdExists(String id) {
        return userAdRepository.findById(id)
                .orElseThrow(() -> new ApiException("Ad not found", HttpStatus.BAD_REQUEST, "AD_NOT_FOUND"));
    }
}
