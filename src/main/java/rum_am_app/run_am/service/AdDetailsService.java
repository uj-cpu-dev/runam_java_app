package rum_am_app.run_am.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import rum_am_app.run_am.dtoresponse.AdDetailsResponse;
import rum_am_app.run_am.dtoresponse.ProfileResponse;
import rum_am_app.run_am.exception.ApiException;
import rum_am_app.run_am.model.UserAd;
import rum_am_app.run_am.repository.UserAdRepository;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class AdDetailsService {
    private final UserAdRepository userAdRepository;
    private final ProfileService profileService;

    public AdDetailsResponse getAdDetails (String adId) {
        UserAd ad = userAdRepository.findById(adId)
                .orElseThrow(() -> new ApiException("Ad not found", HttpStatus.NOT_FOUND, "AD_NOT_FOUND"));

        ProfileResponse sellerProfile = profileService.getProfile(ad.getUserId());

        return AdDetailsResponse.builder()
                // Ad fields
                .id(ad.getId())
                .title(ad.getTitle())
                .price(ad.getPrice())
                .category(ad.getCategory())
                .description(ad.getDescription())
                .location(ad.getLocation())
                .condition(ad.getCondition())
                .images(ad.getImages())
                .views(ad.getViews())
                .messages(ad.getMessages())
                .datePosted(ad.getDatePosted())
                .status(ad.getStatus())
                .dateSold(ad.getDateSold())
                // Seller fields
                .sellerName(sellerProfile.getName())
                .sellerAvatarUrl(sellerProfile.getAvatarUrl())
                .sellerRating(sellerProfile.getRating())
                .sellerItemsSold(sellerProfile.getItemsSold())
                .sellerResponseRate(sellerProfile.getResponseRate())
                .sellerJoinDate(sellerProfile.getJoinDate())
                .sellerReviews(sellerProfile.getReviews())
                .build();
    };
}
