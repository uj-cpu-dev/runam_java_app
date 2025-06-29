package rum_am_app.run_am.controller;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import rum_am_app.run_am.model.UserAd;
import rum_am_app.run_am.service.UserAdService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserAdController {

    private static final Logger logger = LoggerFactory.getLogger(UserAdController.class);

    private final UserAdService userAdService;

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
    public ResponseEntity<UserAd> createAd(
            @RequestPart UserAd userAd,
            @RequestPart(required = false) List<MultipartFile> images) {

        logger.info("Received request to create ad. User ID: {}, Ad ID: {}, Images count: {}",
                userAd.getUserId(), userAd.getId(), images != null ? images.size() : 0);

        if (images != null) {
            images.forEach(img -> logger.info("Image Name: {}, Size: {}, Type: {}",
                    img.getOriginalFilename(), img.getSize(), img.getContentType()));
        }

        assert images != null;
        return ResponseEntity.ok(userAdService.createAdWithImages(userAd, images));
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

}
