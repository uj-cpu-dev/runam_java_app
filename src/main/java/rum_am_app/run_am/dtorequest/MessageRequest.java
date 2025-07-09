package rum_am_app.run_am.dtorequest;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import rum_am_app.run_am.model.Message;
import rum_am_app.run_am.model.User;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageRequest {
    private String content;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MessageDto {
        private String id;
        private String conversationId;
        private User sender;
        private String content;
        private LocalDateTime timestamp;
        private String status;

        public MessageDto(Message message) {
            this.id = message.getId();
            this.conversationId = message.getConversationId();
            this.sender = message.getSender();
            this.content = message.getContent();
            this.timestamp = message.getTimestamp();
            this.status = message.getStatus();
        }
    }
}
