package rum_am_app.run_am.dtoresponse;

import lombok.Builder;
import lombok.Data;
import rum_am_app.run_am.model.UserAd;

import java.util.List;

@Data
@Builder
public class UserLoginResponse {
    private ProfileResponse profile;
    private List<UserAd> ads;
}
