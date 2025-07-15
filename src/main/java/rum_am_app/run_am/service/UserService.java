package rum_am_app.run_am.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.mail.MailException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import rum_am_app.run_am.dtorequest.UserLoginRequest;
import rum_am_app.run_am.dtorequest.UserSignupRequest;
import rum_am_app.run_am.dtorequest.UserUpdateRequest;
import rum_am_app.run_am.exception.ApiException;
import rum_am_app.run_am.model.*;
import rum_am_app.run_am.repository.UserRepository;
import rum_am_app.run_am.repository.VerificationTokenRepository;
import rum_am_app.run_am.util.VerificationToken;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
public class UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final EmailService emailService;

    private final VerificationTokenRepository verificationTokenRepository;

    public UserService(
            UserRepository userRepository, EmailService emailService, VerificationTokenRepository verificationTokenRepository) {
        this.userRepository = userRepository;
        this.emailService = emailService;
        this.verificationTokenRepository = verificationTokenRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
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

        String token = UUID.randomUUID().toString();

        try {
            emailService.sendVerificationEmail(user.getEmail(), token);

            // If email sent successfully, then save user and token
            User savedUser = userRepository.save(user);
            VerificationToken verificationToken = new VerificationToken(
                    token, savedUser, Instant.now().plus(3, ChronoUnit.HOURS)
            );
            verificationTokenRepository.save(verificationToken);

        } catch (MailException e) {
            throw new ApiException("Failed to send verification email", HttpStatus.SERVICE_UNAVAILABLE, "EMAIL_SEND_FAILED");
        }
    }

    public User login(UserLoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ApiException("User not found", HttpStatus.BAD_REQUEST, "USER_NOT_FOUND"));

        if (!user.isEnabled()) {
            throw new ApiException("Email not verified", HttpStatus.UNAUTHORIZED, "EMAIL_NOT_VERIFIED");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new ApiException("Invalid credentials", HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS");
        }

        return user;
    };

    public void updateUser(String userId, UserUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException("User not found", HttpStatus.BAD_REQUEST, "USER_NOT_FOUND"));

        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            if (request.getCurrentPassword() == null ||
                    !passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
                throw new ApiException("Current password is incorrect", HttpStatus.UNAUTHORIZED, "INVALID_CURRENT_PASSWORD");
            }

            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        if (request.getName() != null) {
            user.setName(request.getName());
        }

        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.findByEmail(request.getEmail()).isPresent()) {
                throw new ApiException("New email already in use", HttpStatus.UNAUTHORIZED, "NEW_EMAIL_ALREADY_IN_USE");
            }
            user.setEmail(request.getEmail());
        }

        userRepository.save(user);
    }

    public void resendVerificationEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException("User not found", HttpStatus.NOT_FOUND, "USER_NOT_FOUND"));

        if (user.isEnabled()) {
            throw new ApiException("User already verified", HttpStatus.BAD_REQUEST, "USER_ALREADY_VERIFIED");
        }

        String token = UUID.randomUUID().toString();

        try {
            emailService.sendVerificationEmail(user.getEmail(), token);

            verificationTokenRepository.deleteByUser(user);

            VerificationToken verificationToken = new VerificationToken(
                    token, user, Instant.now().plus(3, ChronoUnit.HOURS)
            );
            verificationTokenRepository.save(verificationToken);

        } catch (MailException e) {
            throw new ApiException("Failed to send verification email", HttpStatus.SERVICE_UNAVAILABLE, "EMAIL_SEND_FAILED");
        }
    }
}

