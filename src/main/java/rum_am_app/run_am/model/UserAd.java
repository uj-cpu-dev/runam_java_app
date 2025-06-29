package rum_am_app.run_am.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "user_ads")
public class UserAd {

    @Id
    private String id;
    private String userId;
    private String title;
    private double price;
    private List<ImageData> images;
    private int views;
    private int messages;
    private Instant datePosted;
    private Instant dateSold;
    private AdStatus status;
    private String category;
    private String location;
    private String condition;
    private String description;

    @Data
    @Getter
    @Setter
    public static class ImageData {
        private String id;
        private String filename;
        private String url;
        private String base64Data;
    }

    public enum AdStatus {
        ACTIVE, SOLD, DRAFT
    }
}
