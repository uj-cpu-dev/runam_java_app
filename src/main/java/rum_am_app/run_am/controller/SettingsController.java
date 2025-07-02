package rum_am_app.run_am.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rum_am_app.run_am.dtorequest.UserSettingsUpdateRequest;
import rum_am_app.run_am.model.UserSettings;
import rum_am_app.run_am.service.SettingsService;

import java.util.Map;

@RestController
@RequestMapping("/api/users/{userId}/settings")
@RequiredArgsConstructor
public class SettingsController {

    private final SettingsService settingsService;

    @GetMapping
    public ResponseEntity<UserSettings> getSettings(@PathVariable String userId) {
        return ResponseEntity.ok(settingsService.getSettings(userId));
    }

    @PatchMapping
    public ResponseEntity<UserSettings> updateSettings(
            @PathVariable String userId,
            @RequestBody UserSettingsUpdateRequest updates) {
        return ResponseEntity.ok(settingsService.updateSettings(userId, updates));
    }

    @PostMapping("/request-data")
    public ResponseEntity<?> requestDataDownload(@PathVariable String userId) {
        return ResponseEntity.accepted().body(Map.of(
                "message", "Data download request received for user: " + userId
        ));
    }

    @DeleteMapping
    public ResponseEntity<?> deleteAccount(@PathVariable String userId) {
        return ResponseEntity.ok(Map.of(
                "message", "Account deletion request received for user: " + userId
        ));
    }
}
