package rum_am_app.run_am.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rum_am_app.run_am.dtorequest.AdFilterRequest;
import rum_am_app.run_am.dtoresponse.RecentActiveAdResponse;
import rum_am_app.run_am.exception.ApiException;
import rum_am_app.run_am.model.UserAd;
import rum_am_app.run_am.repository.UserAdRepository;
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

    private final ImageUploader imageUploader;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

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

    public void createAdWithImages(UserAd userAd, String userId) {
        userAd.setUserId(userId);
        adValidator.validateAdCreation(userAd);
        userAd.setViews(0);
        userAd.setMessages(0);
        userAd.setDatePosted(Instant.now());

        userAdRepository.save(userAd);
    }

    public void updateAdWithImages(UserAd updatedAd, String userId) {
        UserAd existingAd = userAdRepository.findById(updatedAd.getId())
                .orElseThrow(() -> new IllegalArgumentException("Ad not found"));

        if (!existingAd.getUserId().equals(userId)) {
            throw new SecurityException("Unauthorized update attempt");
        }

        existingAd.setTitle(updatedAd.getTitle());
        existingAd.setDescription(updatedAd.getDescription());
        existingAd.setPrice(updatedAd.getPrice());
        existingAd.setCategory(updatedAd.getCategory());
        existingAd.setLocation(updatedAd.getLocation());
        existingAd.setImages(updatedAd.getImages()); //
        existingAd.setStatus(updatedAd.getStatus());
        existingAd.setDatePosted(Instant.now());

        adValidator.validateAdUpdate(existingAd);

        userAdRepository.save(existingAd);
    }

    public List<UserAd> getAllAdsByUserId(String userId) {
        return userAdRepository.findByUserId(userId);
    }

    @Transactional
    public void deleteSingleAd(String adId, String userId) {
        UserAd ad = userAdRepository.findById(adId)
                .orElseThrow(() -> new ApiException("Ad not found", HttpStatus.NOT_FOUND, "AD_NOT_FOUND"));

        if (!ad.getUserId().equals(userId)) {
            throw new ApiException("Not authorized to delete this ad", HttpStatus.FORBIDDEN, "UNAUTHORIZED_OPERATION");
        }

        if (ad.getImages() != null) {
            ad.getImages().forEach(image -> {
                String key = imageUploader.extractS3KeyFromUrl(image.getUrl());
                imageUploader.deleteImageFromS3(key);
            });
        }

        userAdRepository.delete(ad);
    }

    @Transactional
    public void deleteAllAdsByUserId(String userId) {
        // Find all ads for the user
        List<UserAd> userAds = userAdRepository.findByUserId(userId);

        userAds.stream()
                .flatMap(ad -> ad.getImages().stream())
                .forEach(imageData -> {
                    String key = imageUploader.extractS3KeyFromUrl(imageData.getUrl());
                    imageUploader.deleteImageFromS3(key);
                });
        userAdRepository.deleteAll(userAds);
    }

    public List<RecentActiveAdResponse> getRecentActiveAds() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "datePosted"));
        List<UserAd> activeAds = userAdRepository.findTop10ByStatusActiveOrderByDatePostedDesc(pageable);

        return activeAds.stream()
                .map(ad -> RecentActiveAdResponse.builder()
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
                        .build())
                .collect(Collectors.toList());
    }

    public Page<RecentActiveAdResponse> getFilteredAds(AdFilterRequest filterRequest, Pageable pageable) {
        Page<UserAd> filteredAds = userAdRepository.findFilteredAds(filterRequest, pageable);
        return filteredAds.map(this::mapToRecentActiveAdResponse);
    }

    public void deleteAllRecentActiveAds() {
        // Fetch top 10 active ads sorted by datePosted DESC
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "datePosted"));
        List<UserAd> activeAds = userAdRepository.findByStatus("ACTIVE", pageable);

        userAdRepository.deleteAll(activeAds);
    }

    private RecentActiveAdResponse mapToRecentActiveAdResponse(UserAd ad) {
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
                .build();
    }

}
