package rum_am_app.run_am.model;


import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Document(collection = "favorites")
public class Favorite {
    @Id
    private String id;
    private String userId;
    private String adId;
    private Instant favoritedAt;

    @CreatedDate
    private Instant createdAt;
}

