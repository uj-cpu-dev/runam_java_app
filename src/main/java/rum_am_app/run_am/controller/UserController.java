package rum_am_app.run_am.controller;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rum_am_app.run_am.dtorequest.ResendRequest;
import rum_am_app.run_am.dtorequest.UserLoginRequest;
import rum_am_app.run_am.dtoresponse.ApiResponse;
import rum_am_app.run_am.dtoresponse.AuthResponse;
import rum_am_app.run_am.dtorequest.UserSignupRequest;
import rum_am_app.run_am.dtorequest.UserUpdateRequest;
import rum_am_app.run_am.exception.ApiException;
import rum_am_app.run_am.model.User;
import rum_am_app.run_am.repository.UserRepository;
import rum_am_app.run_am.repository.VerificationTokenRepository;
import rum_am_app.run_am.service.UserService;
import rum_am_app.run_am.util.AuthenticationHelper;
import rum_am_app.run_am.util.JwtTokenProvider;
import rum_am_app.run_am.util.VerificationToken;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    private final AuthenticationHelper authHelper;

    private final VerificationTokenRepository verificationTokenRepository;

    private final UserRepository userRepository;

    private final JwtTokenProvider jwtTokenProvider;

    @GetMapping("/verify-email")
    public ResponseEntity<AuthResponse> verifyEmail(@RequestParam String token, HttpServletResponse response) {
        VerificationToken verificationToken = verificationTokenRepository.findByToken(token)
                .orElseThrow(() -> new ApiException("Invalid verification token", HttpStatus.BAD_REQUEST, "INVALID_TOKEN"));

        if (verificationToken.getExpiryDate().isBefore(Instant.now())) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(AuthResponse.builder()
                            .message("Your verification link has expired.")
                            .build());
        }

        User user = verificationToken.getUser();
        user.setEnabled(true);
        userRepository.save(user);
        verificationTokenRepository.delete(verificationToken);

        String accessToken = jwtTokenProvider.createAccessToken(user.getId(), user.getEmail(), List.of("ROLE_USER"));
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getId(), user.getEmail());

        ResponseCookie cookie = ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .secure(true)
                .path("/api/auth/refresh")
                .maxAge(Duration.ofDays(7))
                .sameSite("Strict")
                .build();

        response.setHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        return ResponseEntity.ok(AuthResponse.builder()
                .accessToken(accessToken)
                .email(user.getEmail())
                .name(user.getName())
                .avatarUrl(user.getAvatarUrl())
                .build());
    }

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse> signup(@Valid @RequestBody UserSignupRequest request) {
        try {
            userService.register(request);
            return ApiResponse.create("User registered successfully", HttpStatus.CREATED);
        } catch (Exception e) {
            return ApiResponse.create("Registration failed: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody UserLoginRequest request, HttpServletResponse response) {
        User user = userService.login(request);

        String accessToken = jwtTokenProvider.createAccessToken(user.getId(), user.getEmail(), List.of("ROLE_USER"));
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getId(), user.getEmail());

        ResponseCookie cookie = ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .secure(true)
                .path("/api/auth/refresh") // limit scope
                .maxAge(Duration.ofDays(7))
                .sameSite("Strict")
                .build();

        response.setHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        return ResponseEntity.ok(AuthResponse.builder()
                .accessToken(accessToken)
                .email(user.getEmail())
                .name(user.getName())
                .avatarUrl(user.getAvatarUrl())
                .build());
    }

    @PutMapping("/update-user")
    public ResponseEntity<ApiResponse> updateUser(@RequestBody UserUpdateRequest request) {
        try {
            String userId = authHelper.getAuthenticatedUserId();
            userService.updateUser(userId, request);
            return ApiResponse.create("User updated successfully", HttpStatus.CREATED);
        } catch (Exception e) {
            return ApiResponse.create("User update failed: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/delete-user")
    public ResponseEntity<String> deleteUser() {
        String userId = authHelper.getAuthenticatedUserId();
        userRepository.deleteById(userId);
        return ResponseEntity.ok("User has been deleted");
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(true)
                .path("/api/auth/refresh")
                .maxAge(0) // Expire immediately
                .sameSite("Strict")
                .build();

        response.setHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        return ResponseEntity.ok().build();
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<ApiResponse> resendVerification(@RequestBody ResendRequest request) {
        try {
            userService.resendVerificationEmail(request.getEmail());
            return ApiResponse.create("Verification email resent", HttpStatus.OK);
        } catch (ApiException e) {
            return ApiResponse.create(e.getMessage(), e.getStatus());
        } catch (Exception e) {
            return ApiResponse.create("Failed to resend verification email", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
