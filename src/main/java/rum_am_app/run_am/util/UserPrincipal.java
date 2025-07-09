package rum_am_app.run_am.util;

import lombok.Getter;

import java.security.Principal;
import java.util.List;

@Getter
public class UserPrincipal implements Principal {
    private final String userId;
    private final String email;
    private final List<String> roles;

    public UserPrincipal(String userId, String email, List<String> roles) {
        this.userId = userId;
        this.email = email;
        this.roles = roles;
    }

    @Override
    public String getName() {
        return userId;
    }

}
