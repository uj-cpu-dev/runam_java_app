package rum_am_app.run_am.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "user_settings")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSettings {

    @Id
    private String id;
    private String userId;

    // Notification settings
    private boolean emailNotifications;
    private boolean pushNotifications;
    private boolean smsNotifications;
    private boolean marketingNotifications;
    private boolean newMessageNotifications;
    private boolean itemUpdateNotifications;
    private boolean priceDropNotifications;

    // Privacy settings
    private boolean showPhoneNumber;
    private boolean showEmail;
    private boolean showOnlineStatus;
    private boolean publicProfile;

    @LastModifiedDate
    private Instant lastUpdated;
}
