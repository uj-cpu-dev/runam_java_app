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
import rum_am_app.run_am.model.*;
import rum_am_app.run_am.repository.UserRepository;
import rum_am_app.run_am.repository.VerificationTokenRepository;
import rum_am_app.run_am.util.JwtTokenProvider;
import rum_am_app.run_am.util.VerificationToken;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    private final ProfileService profileService;

    private final JwtTokenProvider jwtTokenProvider;

    private final EmailService emailService;

    private final VerificationTokenRepository verificationTokenRepository;

    public UserService(
            UserRepository userRepository,
            JwtTokenProvider jwtTokenProvider,
            ProfileService profileService, EmailService emailService, VerificationTokenRepository verificationTokenRepository) {
        this.userRepository = userRepository;
        this.emailService = emailService;
        this.verificationTokenRepository = verificationTokenRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
        this.jwtTokenProvider = jwtTokenProvider;
        this.profileService = profileService;
    }

    public void register(UserSignupRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new ApiException("Email already in use", HttpStatus.BAD_REQUEST, "EMAIL_ALREADY_IN_USE");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setName(request.getName());
        user.setJoinDate(Instant.now());
        user.setEnabled(false); // User cannot log in until verified

        User savedUser = userRepository.save(user);

        String token = UUID.randomUUID().toString();
        VerificationToken verificationToken = new VerificationToken(token, savedUser, Instant.now().plus(3, ChronoUnit.HOURS));
        verificationTokenRepository.save(verificationToken);

        emailService.sendVerificationEmail(savedUser.getEmail(), token);
    }

    public AuthResponse login(UserLoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ApiException("User not found", HttpStatus.BAD_REQUEST, "USER_NOT_FOUND"));

        if (!user.isEnabled()) {
            throw new ApiException("Email not verified", HttpStatus.UNAUTHORIZED, "EMAIL_NOT_VERIFIED");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new ApiException("Invalid credentials", HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS");
        }

        // Generate JWT token
        String token = jwtTokenProvider.createToken(
                user.getId(),
                user.getEmail(),
                Collections.singletonList("ROLE_USER") // Add actual roles if you have them
        );

        ProfileResponse profileResponse = profileService.getProfile(user.getId());

        return AuthResponse.builder()
                .token(token)
                .avatarUrl(profileResponse.getAvatarUrl())
                .email(user.getEmail())
                .name(user.getName())
                .build();
    };

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

        if (request.getPassword() != null && !request.getPassword().isBlank()) {
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

