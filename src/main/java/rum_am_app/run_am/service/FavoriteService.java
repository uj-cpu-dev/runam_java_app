package rum_am_app.run_am.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import rum_am_app.run_am.dtoresponse.ProfileResponse;
import rum_am_app.run_am.dtoresponse.RecentActiveAdResponse;
import rum_am_app.run_am.exception.ApiException;
import rum_am_app.run_am.model.Favorite;
import rum_am_app.run_am.model.ProfilePreview;
import rum_am_app.run_am.model.UserAd;
import rum_am_app.run_am.repository.FavoriteRepository;
import rum_am_app.run_am.repository.UserAdRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;

    private final ProfileService profileService;

    private final UserAdRepository userAdRepository;

    public List<RecentActiveAdResponse> getUserFavorites(String userId) {
        List<Favorite> favorites = favoriteRepository.findByUserIdOrderByFavoritedAtDesc(userId);

        return favorites.stream()
                .map(favorite -> {
                    UserAd ad = userAdRepository.findById(favorite.getAdId())
                            .orElseThrow(() -> new ApiException("Ad not found", HttpStatus.NOT_FOUND, "AD_NOT_FOUND"));
                    ProfileResponse profile = profileService.getProfile(ad.getUserId());

                    return RecentActiveAdResponse.builder()
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
                            .favoritedAt(favorite.getFavoritedAt())
                            .seller(ProfilePreview.builder()
                                    .name(profile.getName())
                                    .avatarUrl(profile.getAvatarUrl())
                                    .rating(profile.getRating())
                                    .itemsSold(profile.getItemsSold())
                                    .responseRate(profile.getResponseRate())
                                    .joinDate(profile.getJoinDate())
                                    .build())
                            .build();
                })
                .collect(Collectors.toList());
    }

    public RecentActiveAdResponse toggleFavorite(String userId, String adId) {
        Optional<Favorite> existingFavorite = favoriteRepository.findByUserIdAndAdId(userId, adId);

        // Get the ad from DB (needed in both paths)
        UserAd ad = userAdRepository.findById(adId)
                .orElseThrow(() -> new ApiException("Ad not found", HttpStatus.NOT_FOUND, "AD_NOT_FOUND"));

        ProfileResponse profile = profileService.getProfile(ad.getUserId());

        if (existingFavorite.isPresent()) {
            favoriteRepository.deleteByUserIdAndAdId(userId, adId);

            return buildAdResponse(ad, profile, null); // Not favorited
        } else {
            Favorite newFavorite = Favorite.builder()
                    .userId(userId)
                    .adId(ad.getId())
                    .favoritedAt(Instant.now())
                    .build();

            Favorite savedFavorite = favoriteRepository.save(newFavorite);

            return buildAdResponse(ad, profile, savedFavorite.getFavoritedAt());
        }
    }

    private RecentActiveAdResponse buildAdResponse(UserAd ad, ProfileResponse profile, Instant favoritedAt) {
        return RecentActiveAdResponse.builder()
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
                .favoritedAt(favoritedAt)
                .seller(ProfilePreview.builder()
                        .name(profile.getName())
                        .avatarUrl(profile.getAvatarUrl())
                        .rating(profile.getRating())
                        .itemsSold(profile.getItemsSold())
                        .responseRate(profile.getResponseRate())
                        .joinDate(profile.getJoinDate())
                        .build())
                .build();
    }


}
