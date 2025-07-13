package rum_am_app.run_am.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import rum_am_app.run_am.controller.ConversationController;

import java.time.LocalDateTime;

@Document(collection = "conversations")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Conversation {
    @Id
    private String id;

    @DBRef
    private User participant;

    @DBRef
    private UserAd userAd;

    private String participantId; // <-- denormalized field for indexing
    private String userAdId;      // <-- denormalized field for indexing

    @DBRef
    private ConversationController.ItemDto item;

    private String lastMessage;
    private LocalDateTime timestamp;
    private int unread;
    private String status;

    @Version
    private Long version;

    private User seller;
}
