package rum_am_app.run_am.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rum_am_app.run_am.dtorequest.UserSettingsUpdateRequest;
import rum_am_app.run_am.dtoresponse.UserSettingsResponse;
import rum_am_app.run_am.model.UserSettings;
import rum_am_app.run_am.service.SettingsService;
import rum_am_app.run_am.util.AuthenticationHelper;

import java.util.Map;

@RestController
@RequestMapping("/api/users/settings")
@RequiredArgsConstructor
public class SettingsController {

    private final SettingsService settingsService;

    private final AuthenticationHelper authHelper;

    @GetMapping
    public ResponseEntity<UserSettingsResponse> getSettings() {
        String userId = authHelper.getAuthenticatedUserId();
        UserSettings settings = settingsService.getSettings(userId);
        return ResponseEntity.ok(settingsService.toResponse(settings));
    }

    @PatchMapping
    public ResponseEntity<UserSettingsResponse> updateSettings(
            @RequestBody UserSettingsUpdateRequest updates) {
        String userId = authHelper.getAuthenticatedUserId();
        UserSettings updated = settingsService.updateSettings(userId, updates);
        return ResponseEntity.ok(settingsService.toResponse(updated));
    }

    @PostMapping("/request-data")
    public ResponseEntity<?> requestDataDownload() {
        String userId = authHelper.getAuthenticatedUserId();
        return ResponseEntity.accepted().body(Map.of(
                "message", "Data download request received for user: " + userId
        ));
    }

    @DeleteMapping
    public ResponseEntity<?> deleteAccount() {
        String userId = authHelper.getAuthenticatedUserId();
        return ResponseEntity.ok(Map.of(
                "message", "Account deletion request received for user: " + userId
        ));
    }
}
