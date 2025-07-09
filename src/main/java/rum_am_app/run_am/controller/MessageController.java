package rum_am_app.run_am.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rum_am_app.run_am.dtorequest.MessageRequest;
import rum_am_app.run_am.model.Conversation;
import rum_am_app.run_am.model.Message;
import rum_am_app.run_am.service.MessageService;

import java.util.List;

/*@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {
    private final MessageService messageService;
    private final AuthenticationService authenticationService;

    @GetMapping("/conversations")
    public ResponseEntity<List<Conversation>> getConversations(
            @RequestParam(required = false) String search) {
        String userId = authenticationService.getCurrentUserId();
        return ResponseEntity.ok(messageService.getConversations(userId, search));
    }

    @GetMapping("/conversations/{conversationId}/messages")
    public ResponseEntity<List<Message>> getMessages(
            @PathVariable String conversationId) {
        return ResponseEntity.ok(messageService.getMessages(conversationId));
    }

    @PostMapping("/conversations/{conversationId}/messages")
    public ResponseEntity<Message> sendMessage(
            @PathVariable String conversationId,
            @RequestBody MessageRequest request) {
        String senderId = authenticationService.getCurrentUserId();
        return ResponseEntity.ok(
                messageService.sendMessage(conversationId, senderId, request)
        );
    }

    @PostMapping("/conversations")
    public ResponseEntity<Conversation> createConversation(
            @RequestParam String participantId,
            @RequestParam String itemId) {
        return ResponseEntity.ok(
                messageService.createConversation(participantId, itemId)
        );
    }
}*/
