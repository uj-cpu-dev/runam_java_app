package rum_am_app.run_am.dtoresponse;

import lombok.Builder;
import lombok.Getter;
import rum_am_app.run_am.model.ProfilePreview;
import rum_am_app.run_am.model.UserAd;

import java.time.Instant;
import java.util.List;

@Builder(toBuilder = true)
@Getter
public class RecentActiveAdResponse {
    private String id;
    private String title;
    private Double price;
    private String category;
    private String description;
    private String location;
    private String condition;
    private List<UserAd.ImageData> images;
    private Integer views;
    private Integer messages;
    private Instant datePosted;
    private UserAd.AdStatus status;
    private Instant dateSold;
    private ProfilePreview seller;
    private Instant favoritedAt;
}
