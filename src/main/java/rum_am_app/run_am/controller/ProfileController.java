package rum_am_app.run_am.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import rum_am_app.run_am.dto.ProfileResponse;
import rum_am_app.run_am.dto.UpdateProfileRequest;
import rum_am_app.run_am.service.ProfileService;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    @GetMapping("/{userId}/profile")
    public ResponseEntity<ProfileResponse> getProfile(@PathVariable String userId) {
        return ResponseEntity.ok(profileService.getProfile(userId));
    }

    @PutMapping("/{userId}/profile")
    public ResponseEntity<ProfileResponse> updateProfile(
            @PathVariable String userId,
            @Valid @RequestBody UpdateProfileRequest request) {
        return ResponseEntity.ok(profileService.updateProfile(userId, request));
    }

    @PatchMapping("/{userId}/avatar")
    public ResponseEntity<?> updateAvatar(
            @PathVariable String userId,
            @RequestParam MultipartFile avatar) {
        // Handle avatar upload
        return ResponseEntity.ok().build();
    }
}
