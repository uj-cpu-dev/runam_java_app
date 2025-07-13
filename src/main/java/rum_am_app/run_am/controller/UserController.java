package rum_am_app.run_am.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import rum_am_app.run_am.dtorequest.UserLoginRequest;
import rum_am_app.run_am.dtoresponse.ApiResponse;
import rum_am_app.run_am.dtoresponse.AuthResponse;
import rum_am_app.run_am.dtoresponse.UserResponse;
import rum_am_app.run_am.dtorequest.UserSignupRequest;
import rum_am_app.run_am.dtorequest.UserUpdateRequest;
import rum_am_app.run_am.exception.ApiException;
import rum_am_app.run_am.model.User;
import rum_am_app.run_am.repository.UserRepository;
import rum_am_app.run_am.repository.VerificationTokenRepository;
import rum_am_app.run_am.service.UserService;
import rum_am_app.run_am.util.AuthenticationHelper;
import rum_am_app.run_am.util.VerificationToken;

import java.time.Instant;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    private final AuthenticationHelper authHelper;

    private final VerificationTokenRepository verificationTokenRepository;

    private final UserRepository userRepository;

    @GetMapping("/verify")
    public String verifyEmail(@RequestParam String token, Model model) {
        VerificationToken verificationToken = verificationTokenRepository.findByToken(token)
                .orElseThrow(() -> new ApiException("Invalid verification token", HttpStatus.BAD_REQUEST, "INVALID_TOKEN"));

        if (verificationToken.getExpiryDate().isBefore(Instant.now())) {
            model.addAttribute("message", "Your verification link has expired.");
            model.addAttribute("success", false);
            return "verification-result";
        }

        User user = verificationToken.getUser();
        user.setEnabled(true);
        userRepository.save(user);
        verificationTokenRepository.delete(verificationToken);

        model.addAttribute("message", "Your email has been successfully verified!");
        model.addAttribute("success", true);
        return "verification-result";
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
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody UserLoginRequest request) {
        AuthResponse response = userService.login(request);
        return ResponseEntity.ok()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + response.getToken())
                .body(response);
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
