package rum_am_app.run_am.controller;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.stereotype.Controller;
import rum_am_app.run_am.exception.ApiException;
import rum_am_app.run_am.service.MessageService;
import rum_am_app.run_am.util.AuthenticationHelper;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class MessageWebSocketController {

    private static final Logger logger = LoggerFactory.getLogger(MessageWebSocketController.class);

    private final MessageService messageService;
    private final AuthenticationHelper authHelper;

    @MessageMapping("/chat/{conversationId}")
    @SendTo("/topic/messages/{conversationId}")
    public MessageController.MessageDto handleMessage(
            @DestinationVariable String conversationId,
            SendMessageRequest request) {

        try {
            String userId = authHelper.getAuthenticatedUserId();
            return messageService.sendMessageHttp(conversationId, userId, request.getContent());
        } catch (ApiException e) {
            logger.error("WebSocket message handling failed: {}", e.getMessage());
            throw new RuntimeException(e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error in WebSocket message handling", e);
            throw new RuntimeException("Error processing message");
        }
    }

    @SubscribeMapping("/topic/messages/{conversationId}/initial")
    public List<MessageController.MessageDto> getInitialMessages(
            @DestinationVariable String conversationId) {

        try {
            String userId = authHelper.getAuthenticatedUserId();
            return messageService.getMessages(conversationId, userId);
        } catch (Exception e) {
            logger.error("Failed to load initial messages via WebSocket", e);
            throw new RuntimeException("Error loading messages");
        }
    }

    @Data
    public static class SendMessageRequest {
        private String content;
    }
}
