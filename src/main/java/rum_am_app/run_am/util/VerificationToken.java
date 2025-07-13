package rum_am_app.run_am.util;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import rum_am_app.run_am.model.User;

import java.time.Instant;

@Document(collection = "verification_tokens")
@Getter
@Setter
public class VerificationToken {

    @Id
    private String id;
    private String token;

    public VerificationToken(String token, User user, Instant expiryDate) {
        this.token = token;
        this.user = user;
        this.expiryDate = expiryDate;
    }

    @DBRef
    private User user;

    private Instant expiryDate;


}
