package rum_am_app.run_am.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rum_am_app.run_am.dtorequest.UserLoginRequest;
import rum_am_app.run_am.dtoresponse.ApiResponse;
import rum_am_app.run_am.dtoresponse.AuthResponse;
import rum_am_app.run_am.dtoresponse.UserResponse;
import rum_am_app.run_am.dtorequest.UserSignupRequest;
import rum_am_app.run_am.dtorequest.UserUpdateRequest;
import rum_am_app.run_am.service.UserService;
import rum_am_app.run_am.util.AuthenticationHelper;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    private final AuthenticationHelper authHelper;

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
    public ResponseEntity<UserResponse> updateUser(@PathVariable String userId, @Valid @RequestBody UserUpdateRequest request) {
        UserResponse updatedUser = userService.updateUser(userId, request);
        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping("/delete-user")
    public ResponseEntity<String> deleteUser() {
        String userId = authHelper.getAuthenticatedUserId();
        userService.deleteUser(userId);
        return ResponseEntity.ok("User has been deleted");
    }
}
