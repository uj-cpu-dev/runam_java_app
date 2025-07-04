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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import rum_am_app.run_am.dtorequest.AdFilterRequest;
import rum_am_app.run_am.dtoresponse.RecentActiveAdResponse;
import rum_am_app.run_am.exception.ApiException;
import rum_am_app.run_am.model.UserAd;
import rum_am_app.run_am.service.FavoriteService;
import rum_am_app.run_am.service.UserAdService;

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

    @GetMapping("/user/{userId}/userAds")
    public ResponseEntity<?> getAllUserAds(@PathVariable String userId) {
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

    @GetMapping("/stats/{userId}")
    public ResponseEntity<Map<String, Object>> getUserAdStats(
            @PathVariable String userId) {
        return ResponseEntity.ok(userAdService.getUserAdStats(userId));
    }

    @GetMapping("/{userId}/{status}")
    public ResponseEntity<List<UserAd>> getUserAdsByStatus(
            @PathVariable String userId,
            @PathVariable UserAd.AdStatus status) {
        return ResponseEntity.ok(userAdService.getUserAdsByStatus(userId, status));
    }

    @PostMapping(value = "/post/{userId}/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createAd(
            @RequestPart UserAd userAd,
            @RequestPart(required = false) List<MultipartFile> images) {

        // Validate image sizes before processing
        if (images != null) {
            for (MultipartFile image : images) {
                if (image.getSize() > 10 * 1024 * 1024) { // 10MB
                    return ResponseEntity.badRequest()
                            .body("Image " + image.getOriginalFilename() +
                                    " exceeds maximum size of 10MB");
                }

                logger.info("Image Name: {}, Size: {}, Type: {}",
                        image.getOriginalFilename(),
                        image.getSize(),
                        image.getContentType());
            }
        }

        try {
            UserAd createdAd = userAdService.createAdWithImages(userAd, images);
            return ResponseEntity.ok(createdAd);
        } catch (Exception e) {
            logger.error("Error creating ad", e);
            return ResponseEntity.internalServerError()
                    .body("Error processing your request");
        }
    }

    @PutMapping(value = "/put/{userId}/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UserAd> updateAd(
            @PathVariable String id,
            @RequestPart UserAd userAd,
            @RequestPart(required = false) List<MultipartFile> newImages) {

        return ResponseEntity.ok(userAdService.updateAdWithImages(id, userAd, newImages));
    }

    @DeleteMapping("/delete/{userId}/{adId}/userAd")
    public ResponseEntity<Map<String, String>> deleteSingleAd(
            @PathVariable String userId,
            @PathVariable String adId) {

        userAdService.deleteSingleAd(adId, userId);

        return ResponseEntity.ok(Map.of(
                "message",
                String.format("Ad with ID %s deleted successfully for user %s.", adId, userId)
        ));
    }

    @DeleteMapping("/delete/{userId}/userAds")
    public ResponseEntity<Map<String, String>> deleteAllUserAds(@PathVariable String userId) {

        userAdService.deleteAllAdsByUserId(userId);

        return ResponseEntity.ok(Map.of(
                "message",
                String.format("All ads deleted successfully for user %s.", userId)
        ));
    }

    @GetMapping("/recent")
    public ResponseEntity<List<RecentActiveAdResponse>> getRecentActiveAds() {
        List<RecentActiveAdResponse> recentAds = userAdService.getRecentActiveAds();
        return ResponseEntity.ok(recentAds);
    }

    @GetMapping("/{userId}/favorites")
    public List<RecentActiveAdResponse> getUserFavorites(
            @PathVariable String userId) {
        return favoriteService.getUserFavorites(userId);
    }

    @PostMapping("/{userId}/{adId}/toggle-favorites")
    public ResponseEntity<?> toggleFavorite(
            @PathVariable String userId,
            @PathVariable String adId) {
        try {
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
