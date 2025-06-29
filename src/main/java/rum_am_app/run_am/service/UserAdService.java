package rum_am_app.run_am.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import rum_am_app.run_am.exception.ApiException;
import rum_am_app.run_am.model.UserAd;
import rum_am_app.run_am.repository.UserAdRepository;
import rum_am_app.run_am.util.AdUpdateHelper;
import rum_am_app.run_am.util.AdValidator;
import rum_am_app.run_am.util.ImageUploader;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserAdService {

    private final UserAdRepository userAdRepository;

    private final AdValidator adValidator;

    private final AdUpdateHelper adUpdateHelper;

    private final ImageUploader imageUploader;

    public List<UserAd> getUserAdsByStatus(String userId, UserAd.AdStatus status) {
        return userAdRepository.findByUserIdAndStatus(userId, status);
    }

    public Map<String, Object> getUserAdStats(String userId) {
        long activeCount = userAdRepository.countByUserIdAndStatus(userId, UserAd.AdStatus.ACTIVE);
        long soldCount = userAdRepository.countByUserIdAndStatus(userId, UserAd.AdStatus.SOLD);
        long draftCount = userAdRepository.countByUserIdAndStatus(userId, UserAd.AdStatus.DRAFT);
        long totalCount = userAdRepository.countByUserId(userId);

        List<UserAd> allAds = userAdRepository.findByUserId(userId);
        int totalViews = allAds.stream().mapToInt(UserAd::getViews).sum();
        int totalMessages = allAds.stream().mapToInt(UserAd::getMessages).sum();
        double totalEarnings = allAds.stream()
                .filter(ad -> ad.getStatus() == UserAd.AdStatus.SOLD)
                .mapToDouble(UserAd::getPrice)
                .sum();

        return Map.of(
                "activeCount", activeCount,
                "soldCount", soldCount,
                "draftCount", draftCount,
                "totalCount", totalCount,
                "totalViews", totalViews,
                "totalMessages", totalMessages,
                "totalEarnings", totalEarnings
        );
    }

    public UserAd createAdWithImages(UserAd userAd, List<MultipartFile> imageFiles) {
        adValidator.validateAdCreation(userAd);

        List<UserAd.ImageData> imageData = processNewImages(imageFiles, userAd.getUserId());
        UserAd newAd = initializeNewAd(userAd, imageData);

        return userAdRepository.save(newAd);
    }

    private List<UserAd.ImageData> processNewImages(List<MultipartFile> newImages, String userId) {
        if (newImages == null || newImages.isEmpty()) {
            return new ArrayList<>();
        }

        return newImages.stream()
                .map(file -> {
                    String url = imageUploader.uploadImageToS3(file, userId);
                    UserAd.ImageData imageData = new UserAd.ImageData();
                    imageData.setId(UUID.randomUUID().toString());
                    imageData.setFilename(file.getOriginalFilename());
                    imageData.setUrl(url);
                    return imageData;
                })
                .collect(Collectors.toList());
    }

    private UserAd initializeNewAd(UserAd userAd, List<UserAd.ImageData> imageData) {
        userAd.setImages(imageData);
        userAd.setViews(0);
        userAd.setMessages(0);
        userAd.setDatePosted(Instant.now());
        userAd.setStatus(UserAd.AdStatus.ACTIVE);
        return userAd;
    }

    public UserAd updateAdWithImages(String id, UserAd updatedAd, List<MultipartFile> newImages) {
        log.info("Updating ad with ID={}", id); // Single important log

        UserAd existingAd = userAdRepository.findById(id)
                .orElseThrow(() -> new ApiException("Ad not found with id-" + id, HttpStatus.BAD_REQUEST, "AD_NOT_FOUND"));

        // Update basic fields
        adUpdateHelper.updateBasicFields(existingAd, updatedAd);

        // Process images
        List<UserAd.ImageData> finalImages = new ArrayList<>();
        Set<String> imagesToDelete = adUpdateHelper.processImageUpdates(
                existingAd, updatedAd, newImages, finalImages);

        existingAd.setImages(finalImages);

        // Handle status changes
        if (updatedAd.getStatus() == UserAd.AdStatus.SOLD &&
                existingAd.getStatus() != UserAd.AdStatus.SOLD) {
            existingAd.setStatus(UserAd.AdStatus.SOLD);
            existingAd.setDateSold(Instant.now());
        }

        UserAd savedAd = userAdRepository.save(existingAd);
        adUpdateHelper.cleanupImages(imagesToDelete);

        return savedAd;
    }

    public List<UserAd> getAllAdsByUserId(String userId) {
        return userAdRepository.findByUserId(userId);
    }


    @Transactional
    public void deleteSingleAd(String adId, String userId) {
        // Find the specific ad
        UserAd ad = userAdRepository.findById(adId)
                .orElseThrow(() -> new ApiException("Ad not found", HttpStatus.NOT_FOUND, "AD_NOT_FOUND"));

        if (!ad.getUserId().equals(userId)) {
            throw new ApiException(
                    "Not authorized to delete this ad",
                    HttpStatus.FORBIDDEN,
                    "UNAUTHORIZED_OPERATION"
            );
        }

        ad.getImages().forEach(imageData ->
                imageUploader.deleteImageFromS3(imageData.getUrl()));
        userAdRepository.delete(ad);
    }
    @Transactional
    public void deleteAllAdsByUserId(String userId) {
        // Find all ads for the user
        List<UserAd> userAds = userAdRepository.findByUserId(userId);

        userAds.stream()
                .flatMap(ad -> ad.getImages().stream())
                .forEach(imageData -> imageUploader.deleteImageFromS3(imageData.getUrl()));

        userAdRepository.deleteAll(userAds);
    }
}
