package rum_am_app.run_am.controller;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import rum_am_app.run_am.dtorequest.UpdateProfileRequest;
import rum_am_app.run_am.service.ProfileService;
import rum_am_app.run_am.util.AuthenticationHelper;

@RestController
@RequestMapping("/api/users/profile")
@RequiredArgsConstructor
public class ProfileController {

    private static final Logger logger = LoggerFactory.getLogger(ProfileController.class);

    private final ProfileService profileService;

    private final AuthenticationHelper authHelper;

    @GetMapping
    public ResponseEntity<?> getProfile() {

        try {
            String userId = authHelper.getAuthenticatedUserId();
            return ResponseEntity.ok(profileService.getProfile(userId));
        } catch (Exception e) {
            logger.error("Error getting user profile details", e);
            return ResponseEntity.internalServerError()
                    .body("Error processing your request");
        }
    }

    @PutMapping
    public ResponseEntity<?> updateProfile(
            @RequestBody UpdateProfileRequest request) {

        try {
            String userId = authHelper.getAuthenticatedUserId();
            return ResponseEntity.ok(profileService.updateProfile(userId, request));
        } catch (Exception e) {
            logger.error("Error updating user profile details", e);
            return ResponseEntity.internalServerError()
                    .body("Error processing your request");
        }
    }
}
