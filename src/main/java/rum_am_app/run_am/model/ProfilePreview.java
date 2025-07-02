package rum_am_app.run_am.model;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class ProfilePreview {
    private String name;
    private String avatarUrl;
    private Double rating;
    private Integer itemsSold;
    private Double responseRate;
    private String joinDate;
}
