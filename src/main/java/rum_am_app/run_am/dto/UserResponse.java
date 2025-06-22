package rum_am_app.run_am.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class UserResponse {
    private String id;
    private String email;
    private String name;
    private Instant createdAt;
}
