package rum_am_app.run_am.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import rum_am_app.run_am.dto.UserSignupRequest;
import rum_am_app.run_am.exception.UserAlreadyExistsException;
import rum_am_app.run_am.model.User;
import rum_am_app.run_am.repository.UserRepository;

import java.time.Instant;

@Slf4j
@Service
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    public User register(UserSignupRequest request) {
        log.info("Attempting to register user with email: {}", request.getEmail());

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            log.warn("Registration failed - Email already in use: {}", request.getEmail());
            throw new UserAlreadyExistsException("Email already in use");
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .joinDate(Instant.now())
                .build();

        User savedUser = userRepository.save(user);
        log.info("User registered successfully: {}", savedUser.getEmail());
        return savedUser;
    }
}

