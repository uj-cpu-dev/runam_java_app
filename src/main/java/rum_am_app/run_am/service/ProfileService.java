package rum_am_app.run_am.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import rum_am_app.run_am.dtoresponse.ProfileResponse;
import rum_am_app.run_am.dtorequest.UpdateProfileRequest;
import rum_am_app.run_am.exception.ApiException;
import rum_am_app.run_am.model.User;
import rum_am_app.run_am.model.UserAd;
import rum_am_app.run_am.repository.UserAdRepository;
import rum_am_app.run_am.repository.UserRepository;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;


@Service
@RequiredArgsConstructor
public class ProfileService {

    private final UserRepository userRepository;

    private final UserAdRepository userAdRepository;

    public ProfileResponse getProfile(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException("User not found", HttpStatus.BAD_REQUEST, "USER_NOT_FOUND"));

        List<UserAd> userAds = userAdRepository.findByUserId(userId);

        return ProfileResponse.builder()
                .id(user.getId())
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

    private String formatJoinDate(Instant joinDate) {
        return DateTimeFormatter.ofPattern("MMMM yyyy")
                .withZone(ZoneId.systemDefault())
                .format(joinDate);
    }
    private ProfileResponse mapToProfileResponse(User user) {
        return ProfileResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .location(user.getLocation())
                .bio(user.getBio())
                .joinDate(formatJoinDate(user.getJoinDate()))
                .avatarUrl(user.getAvatarUrl())
                .rating(user.getRating())
                .itemsSold(user.getItemsSold())
                .activeListings(user.getActiveListings())
                .responseRate(user.getResponseRate())
                .emailVerified(user.isEmailVerified())
                .phoneVerified(user.isPhoneVerified())
                .isQuickResponder(user.isQuickResponder())
                .isTopSeller(user.isTopSeller())
                .reviews(user.getReviews())
                .build();
    }

}
