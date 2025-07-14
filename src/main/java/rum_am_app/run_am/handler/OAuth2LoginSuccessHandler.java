package rum_am_app.run_am.handler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;
import rum_am_app.run_am.model.User;
import rum_am_app.run_am.repository.UserRepository;
import rum_am_app.run_am.util.JwtTokenProvider;

import java.io.IOException;
import java.util.Collections;

@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    @Value("${frontend.redirect-uri}")
    private String redirectUri;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        OAuth2User oauthUser = (OAuth2User) authentication.getPrincipal();

        String email = oauthUser.getAttribute("email");
        String name = oauthUser.getAttribute("name");
        String picture = oauthUser.getAttribute("picture");
        String provider = extractProviderFromRequest(request); // we'll define this helper

        if (email == null) {
            log.error("OAuth2 success handler failed: email not found in OAuth2 response.");
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Email not found in OAuth2 user");
            return;
        }

        // Find or create user
        User user = userRepository.findByEmail(email).orElseGet(() -> {
            log.info("User not found. Creating new user: {}", email);
            User newUser = User.builder()
                    .email(email)
                    .name(name)
                    .avatarUrl(picture)
                    .provider(provider)
                    .build();
            return userRepository.save(newUser);
        });

        // Generate token
        String token = jwtTokenProvider.createAccessToken(user.getId().toString(), user.getEmail(), Collections.singletonList("ROLE"));

        // Redirect to frontend with token and email
        String targetUrl = UriComponentsBuilder.fromUriString(redirectUri)
                .queryParam("token", token)
                .queryParam("email", email)
                .queryParam("name", name)
                .build().toUriString();

        log.info("Redirecting to frontend with token for user: {}", email);
        response.sendRedirect(targetUrl);
    }

    private String extractProviderFromRequest(HttpServletRequest request) {
        String referer = request.getHeader("Referer");
        if (referer != null && referer.contains("google")) return "google";
        if (referer != null && referer.contains("facebook")) return "facebook";
        return "unknown";
    }
}



