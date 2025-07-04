package rum_am_app.run_am.dtorequest;

import lombok.Builder;
import lombok.Data;
import rum_am_app.run_am.model.UserAd;

import java.time.Instant;

@Data
@Builder
public class AdFilterRequest {
    private String category;
    private String location;
    private String condition;
    private Double minPrice;
    private Double maxPrice;
    private UserAd.AdStatus status;
    private String searchQuery;
    private Instant postedAfter;

    @Builder.Default
    private SortDirection sortDirection = SortDirection.DESC;

    @Builder.Default
    private String sortBy = "datePosted";

    public enum SortDirection {
        ASC, DESC
    }
}
