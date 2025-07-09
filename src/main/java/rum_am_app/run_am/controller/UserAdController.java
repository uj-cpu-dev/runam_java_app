package rum_am_app.run_am.controller;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import rum_am_app.run_am.dtorequest.AdFilterRequest;
import rum_am_app.run_am.dtoresponse.AdDetailsResponse;
import rum_am_app.run_am.dtoresponse.RecentActiveAdResponse;
import rum_am_app.run_am.exception.ApiException;
import rum_am_app.run_am.model.UserAd;
import rum_am_app.run_am.service.AdDetailsService;
import rum_am_app.run_am.service.FavoriteService;
import rum_am_app.run_am.service.UserAdService;
import rum_am_app.run_am.util.AuthenticationHelper;
import rum_am_app.run_am.util.UserPrincipal;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserAdController {

    private static final Logger logger = LoggerFactory.getLogger(UserAdController.class);

    private final UserAdService userAdService;

    private final FavoriteService favoriteService;

    private final AdDetailsService adDetailsService;

    private final AuthenticationHelper authHelper;

    @GetMapping("/user/userAds")
    public ResponseEntity<?> getAllUserAds() {
        String userId = authHelper.getAuthenticatedUserId();
        List<UserAd> ads = userAdService.getAllAdsByUserId(userId);

        if (ads.isEmpty()) {
            return ResponseEntity.status(HttpStatus.OK).body(Map.of(
                    "message", "No ads found for this user.",
                    "ads", ads
            ));
        }

        return ResponseEntity.ok(Map.of(
                "message", "User ads retrieved successfully.",
                "ads", ads
        ));
    }

    @GetMapping("/filter")
    public ResponseEntity<List<RecentActiveAdResponse>> getFilteredAds(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String condition,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) UserAd.AdStatus status,
            @RequestParam(required = false) String searchQuery,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant postedAfter,
            @RequestParam(defaultValue = "DESC") AdFilterRequest.SortDirection sortDirection,
            @RequestParam(defaultValue = "datePosted") String sortBy,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {

        AdFilterRequest filterRequest = AdFilterRequest.builder()
                .category(category)
                .location(location)
                .condition(condition)
                .minPrice(minPrice)
                .maxPrice(maxPrice)
                .status(status)
                .searchQuery(searchQuery)
                .postedAfter(postedAfter)
                .sortDirection(sortDirection)
                .sortBy(sortBy)
                .build();

        Sort sort = Sort.by(
                filterRequest.getSortDirection() == AdFilterRequest.SortDirection.ASC ?
                        Sort.Direction.ASC : Sort.Direction.DESC,
                filterRequest.getSortBy()
        );

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<RecentActiveAdResponse> response = userAdService.getFilteredAds(filterRequest, pageable);

        return ResponseEntity.ok(response.getContent());
    }

    @PostMapping("/post-ad")
    public ResponseEntity<?> createAd(
            @RequestBody UserAd userAd) {
        try {
            String userId = authHelper.getAuthenticatedUserId();
            UserAd createdAd = userAdService.createAdWithImages(userAd, userId);
            return ResponseEntity.ok(createdAd);
        } catch (Exception e) {
            logger.error("Error creating ad", e);
            return ResponseEntity.internalServerError()
                    .body("Error processing your request");
        }
    }

    @PutMapping("/update-ad")
    public ResponseEntity<?> updateAd(
            @RequestBody UserAd userAd) {
        try {
            String userId = authHelper.getAuthenticatedUserId();
            UserAd updated = userAdService.updateAdWithImages(userAd, userId);
            return ResponseEntity.ok(updated);
        } catch (SecurityException se) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(se.getMessage());
        } catch (Exception e) {
            logger.error("Error updating ad", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error updating ad");
        }
    }

    @DeleteMapping("/delete/{adId}/userAd")
    public ResponseEntity<?> deleteSingleAd(
            @PathVariable String adId) {

        try {
            String userId = authHelper.getAuthenticatedUserId();
            userAdService.deleteSingleAd(adId, userId);
            return ResponseEntity.ok(Map.of(
                    "message",
                    String.format("Ad with ID %s deleted successfully for user %s.", adId, userId)
            ));
        } catch (Exception e) {
            logger.error("Error deleting ad", e);
            return ResponseEntity.internalServerError()
                    .body("Error processing your request");
        }
    }

    @DeleteMapping("/delete/userAds")
    public ResponseEntity<?> deleteAllUserAds() {
        try {
            String userId = authHelper.getAuthenticatedUserId();
            userAdService.deleteAllAdsByUserId(userId);
            return ResponseEntity.ok(Map.of(
                    "message",
                    "All ads deleted successfully"
            ));
        } catch (Exception e) {
            logger.error("Error deleting all ad", e);
            return ResponseEntity.internalServerError()
                    .body("Error processing your request");
        }
    }

    @GetMapping("/recent")
    public ResponseEntity<List<RecentActiveAdResponse>> getRecentActiveAds() {
        List<RecentActiveAdResponse> recentAds = userAdService.getRecentActiveAds();
        return ResponseEntity.ok(recentAds);
    }

    @GetMapping("/{adId}/userAd")
    public ResponseEntity<AdDetailsResponse> getUserAdById(@PathVariable String adId) {
        return ResponseEntity.ok(adDetailsService.getAdDetails(adId));
    }

    @GetMapping("/favorites")
    public ResponseEntity<?> getUserFavorites() {
        try {
            String userId = authHelper.getAuthenticatedUserId();
            List<RecentActiveAdResponse> favorites = favoriteService.getUserFavorites(userId);
            return ResponseEntity.ok(favorites);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to retrieve favorites: " + e.getMessage());
        }
    }

    @PostMapping("/{adId}/toggle-favorites")
    public ResponseEntity<?> toggleFavorite(
            @PathVariable String adId) {

        try {
            String userId = authHelper.getAuthenticatedUserId();
            RecentActiveAdResponse response = favoriteService.toggleFavorite(userId, adId);

            String message = (response.getFavoritedAt() != null)
                    ? "Ad successfully added to your favorites"
                    : "Ad removed from your favorites";

            return ResponseEntity.ok(
                    Map.of(
                            "message", message
                    )
            );
        } catch (ApiException ex) {
            return ResponseEntity.status(ex.getStatus()).body(
                    Map.of(
                            "error", ex.getMessage(),
                            "code", ex.getErrorCode()
                    )
            );
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    Map.of(
                            "error", "An unexpected error occurred",
                            "details", ex.getMessage()
                    )
            );
        }
    }

    @DeleteMapping("/recent/active")
    public ResponseEntity<Map<String, String>> deleteRecentActiveAds() {
        userAdService.deleteAllRecentActiveAds();
        return ResponseEntity.ok(
                Map.of(
                        "message", "All recent ads deleted"
                )
        );
    }

}
