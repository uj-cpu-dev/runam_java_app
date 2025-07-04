package rum_am_app.run_am.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rum_am_app.run_am.dtoresponse.ProfileResponse;
import rum_am_app.run_am.dtorequest.UpdateProfileRequest;
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
            @RequestBody UpdateProfileRequest request) {
        return ResponseEntity.ok(profileService.updateProfile(userId, request));
    }
}
