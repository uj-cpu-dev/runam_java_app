package rum_am_app.run_am.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "users")
public class User {
    @Id
    private String id;

    private String name;
    private String email;
    private String password;
    private Instant joinDate;

    private String phone;
    private String location;
    private String bio;
    private String avatarUrl;

    // Profile statistics
    private double rating;
    private int itemsSold;
    private int activeListings;
    private double responseRate;

    // Verification status
    private boolean emailVerified;
    private boolean phoneVerified;

    // Social media links (optional)
    private Map<String, String> socialLinks;

    // Preferences
    private List<String> favoriteCategories;

}
