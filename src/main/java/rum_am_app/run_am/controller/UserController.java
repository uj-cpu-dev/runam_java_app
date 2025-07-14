package rum_am_app.run_am.controller;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
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
    public ResponseEntity<ApiResponse> verifyEmail(@RequestParam String token) {
        VerificationToken verificationToken = verificationTokenRepository.findByToken(token)
                .orElseThrow(() -> new ApiException("Invalid verification token", HttpStatus.BAD_REQUEST, "INVALID_TOKEN"));

        if (verificationToken.getExpiryDate().isBefore(Instant.now())) {
            return ApiResponse.create("Your verification link has expired.", HttpStatus.BAD_REQUEST);
        }

        User user = verificationToken.getUser();
        user.setEnabled(true);
        userRepository.save(user);
        verificationTokenRepository.delete(verificationToken);

        return ApiResponse.create("Your email has been successfully verified!", HttpStatus.OK);
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
        User user = userService.login(request); // your existing logic

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
}
