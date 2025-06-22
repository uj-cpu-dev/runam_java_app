package rum_am_app.run_am.controller;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import rum_am_app.run_am.dto.UserResponse;
import rum_am_app.run_am.dto.UserSignupRequest;
import rum_am_app.run_am.exception.UserAlreadyExistsException;
import rum_am_app.run_am.model.User;
import rum_am_app.run_am.service.UserService;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@Valid @RequestBody UserSignupRequest request) {
        try {
            logger.info("Signup attempt for email: {}", request.getEmail());
            UserResponse newUser = userService.register(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(newUser);
        } catch (UserAlreadyExistsException e) {
            logger.warn("Signup failed - email already exists: {}", request.getEmail());
            throw e; // Let GlobalExceptionHandler process it
        }
    }
}
