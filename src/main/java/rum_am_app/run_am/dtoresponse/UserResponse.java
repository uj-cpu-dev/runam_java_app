package rum_am_app.run_am.dtoresponse;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class UserResponse {
    private ProfileResponse profile;
    private List<UserAdResponse> ads; // 👈 change to List
}
