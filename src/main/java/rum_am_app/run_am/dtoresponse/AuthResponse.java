package rum_am_app.run_am.dtoresponse;

import lombok.Builder;
import lombok.Data;
import rum_am_app.run_am.model.UserAd;
import rum_am_app.run_am.model.UserSettings;

import java.util.List;

@Data
@Builder
public class AuthResponse {
    private String accessToken;
    private String refreshToken;
    private String avatarUrl;
    private String email;
    private String name;
    private String message;
}
