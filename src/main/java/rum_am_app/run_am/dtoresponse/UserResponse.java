package rum_am_app.run_am.dtoresponse;

import lombok.Builder;
import lombok.Data;
import rum_am_app.run_am.model.UserSettings;

import java.util.List;

@Data
@Builder
public class UserResponse {
    private ProfileResponse profile;
    private List<UserAdResponse> ads;
    private List<RecentActiveAdResponse> favorites;
    private UserSettings settings;
}
