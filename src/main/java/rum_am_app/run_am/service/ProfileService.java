package rum_am_app.run_am.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import rum_am_app.run_am.dtoresponse.ProfileResponse;
import rum_am_app.run_am.dtorequest.UpdateProfileRequest;
import rum_am_app.run_am.exception.ApiException;
import rum_am_app.run_am.model.User;
import rum_am_app.run_am.model.UserAd;
import rum_am_app.run_am.repository.UserAdRepository;
import rum_am_app.run_am.repository.UserRepository;
import rum_am_app.run_am.util.ImageUploader;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;


@Service
@RequiredArgsConstructor
public class ProfileService {

    private final UserRepository userRepository;

    private final UserAdRepository userAdRepository;

    private final UserAdService userAdService;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    private final ImageUploader imageUploader;

    public ProfileResponse getProfile(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException("User not found", HttpStatus.BAD_REQUEST, "USER_NOT_FOUND"));

        List<UserAd> userAds = userAdRepository.findByUserId(userId);

        return ProfileResponse.builder()
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .location(user.getLocation())
                .bio(user.getBio())
                .joinDate(formatJoinDate(user.getJoinDate()))
                .avatarUrl(user.getAvatarUrl())
                .rating(user.getRating())
                .itemsSold(user.getItemsSold())
                .activeListings((int) userAds.stream()
                        .filter(ad -> ad.getStatus() == UserAd.AdStatus.ACTIVE)
                        .count())
                .responseRate(user.getResponseRate())
                .emailVerified(user.isEmailVerified())
                .phoneVerified(user.isPhoneVerified())
                .build();

    }

    public ProfileResponse updateProfile(String userId, UpdateProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException("User not found", HttpStatus.BAD_REQUEST, "USER_NOT_FOUND"));

        user.setAvatarUrl(request.getAvatarUrl());

        if (request.getName() != null) {
            user.setName(request.getName());
        }
        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.findByEmail(request.getEmail()).isPresent()) {
                throw new ApiException("New email already in use", HttpStatus.UNAUTHORIZED, "NEW_EMAIL_ALREADY_IN_USE");
            }
            user.setEmail(request.getEmail());
            user.setEmailVerified(false);
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
            user.setPhoneVerified(false);
        }
        if (request.getLocation() != null) {
            user.setLocation(request.getLocation());
        }
        if (request.getBio() != null) {
            user.setBio(request.getBio());
        }

        User updatedUser = userRepository.save(user);
        return mapToProfileResponse(updatedUser);
    }

    public String formatJoinDate(Instant joinDate) {
        if (joinDate == null) {
            return "N/A";
        }
        return DateTimeFormatter.ofPattern("dd MMM yyyy")
                .withZone(ZoneId.systemDefault())
                .format(joinDate);
    }
    private ProfileResponse mapToProfileResponse(User user) {
        List<UserAd> userAds = userAdService.getAllAdsByUserId(user.getId());
        return ProfileResponse.builder()
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .location(user.getLocation())
                .bio(user.getBio())
                .joinDate(formatJoinDate(user.getJoinDate()))
                .avatarUrl(user.getAvatarUrl())
                .rating(user.getRating())
                .itemsSold(user.getItemsSold())
                .activeListings((int) userAds.stream()
                        .filter(ad -> ad.getStatus() == UserAd.AdStatus.ACTIVE)
                        .count())
                .responseRate(user.getResponseRate())
                .emailVerified(user.isEmailVerified())
                .phoneVerified(user.isPhoneVerified())
                .isQuickResponder(user.isQuickResponder())
                .isTopSeller(user.isTopSeller())
                .reviews(user.getReviews())
                .build();
    }

    public String uploadAvatar(MultipartFile avatar, String userId) {
        if (avatar == null || avatar.isEmpty()) {
            return null;
        }

        return imageUploader.uploadImageToS3(avatar, userId);
    }

}
