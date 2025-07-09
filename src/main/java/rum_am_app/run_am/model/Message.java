package rum_am_app.run_am.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "messages")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Message {
    @Id
    private String id;

    private String conversationId;

    @DBRef
    private User sender; // Reference to User collection

    private String content;
    private LocalDateTime timestamp;
    private String status; // "sent", "delivered", "read"

    @Version
    private Long version;
}
