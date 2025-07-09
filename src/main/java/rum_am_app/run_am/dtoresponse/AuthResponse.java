package rum_am_app.run_am.dtoresponse;

import lombok.Builder;
import lombok.Data;
import rum_am_app.run_am.model.UserAd;
import rum_am_app.run_am.model.UserSettings;

import java.util.List;

@Data
@Builder
public class AuthResponse {
    private String token;
    private String avatarUrl;
    private String email;
    private String name;
}
