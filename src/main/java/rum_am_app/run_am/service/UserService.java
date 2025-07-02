package rum_am_app.run_am.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import rum_am_app.run_am.dtorequest.UserLoginRequest;
import rum_am_app.run_am.dtoresponse.*;
import rum_am_app.run_am.dtorequest.UserSignupRequest;
import rum_am_app.run_am.dtorequest.UserUpdateRequest;
import rum_am_app.run_am.exception.ApiException;
import rum_am_app.run_am.model.Favorite;
import rum_am_app.run_am.model.User;
import rum_am_app.run_am.model.UserAd;
import rum_am_app.run_am.model.UserSettings;
import rum_am_app.run_am.repository.UserRepository;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final FavoriteService favoriteService;
    private final UserAdService userAdService;
    private final SettingsService settingsService;

    public UserService(UserRepository userRepository, UserAdService userAdService, FavoriteService favoriteService, SettingsService settingsService) {
        this.userRepository = userRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
        this.userAdService = userAdService;
        this.favoriteService = favoriteService;
        this.settingsService = settingsService;
    }

    public void register(UserSignupRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new ApiException("Email already in use", HttpStatus.NOT_FOUND, "EMAIL_ALREADY_IN_USE");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setName(request.getName());
        user.setJoinDate(Instant.now());

        User savedUser = userRepository.save(user);
        logger.info("New user created with ID: {}", savedUser.getId());

        mapToUserResponse(savedUser);
    }

    public UserResponse login(UserLoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ApiException("User not found", HttpStatus.BAD_REQUEST, "USER_NOT_FOUND"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new ApiException("Invalid credentials", HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS");
        }

        logger.info("User logged in: {}", user.getEmail());

        ProfileResponse profile = ProfileResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .location(user.getLocation())
                .bio(user.getBio())
                .joinDate(user.getJoinDate().toString())
                .avatarUrl(user.getAvatarUrl())
                .rating(user.getRating())
                .itemsSold(user.getItemsSold())
                .activeListings(user.getActiveListings())
                .responseRate(user.getResponseRate())
                .emailVerified(user.isEmailVerified())
                .phoneVerified(user.isPhoneVerified())
                .reviews(user.getReviews())
                .isQuickResponder(user.isQuickResponder())
                .isTopSeller(user.isTopSeller())
                .build();

        List<UserAd> userAds = userAdService.getAllAdsByUserId(user.getId());

        List<UserAdResponse> adResponses = userAds.stream()
                .map(UserAdResponse::fromEntity)
                .collect(Collectors.toList());

        List<RecentActiveAdResponse> favoriteResponses = favoriteService.getUserFavorites(user.getId());

        UserSettings userSettings = settingsService.getSettings(user.getId());

        return UserResponse.builder()
                .profile(profile)
                .ads(adResponses)
                .favorites(favoriteResponses)
                .settings(userSettings)
                .build();
    }

    public UserResponse updateUser(String userId, UserUpdateRequest request) {
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
        }

        if (request.getPassword() != null) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        User updatedUser = userRepository.save(user);
        logger.info("User updated: {}", updatedUser.getId());
        return mapToUserResponse(updatedUser);
    }

    public void deleteUser(String userId) {
        if (!userRepository.existsById(userId)) {
            throw new ApiException("User not found", HttpStatus.NOT_FOUND, "USER_NOT_FOUND");
        }

        userRepository.deleteById(userId);
        logger.info("User deleted: {}", userId);
    }


    private UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .build();
    }
}

