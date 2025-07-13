package rum_am_app.run_am.controller;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rum_am_app.run_am.model.Conversation;
import rum_am_app.run_am.repository.ConversationRepository;
import rum_am_app.run_am.service.ConversationService;
import rum_am_app.run_am.util.AuthenticationHelper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import rum_am_app.run_am.exception.ApiException;

@RestController
@RequestMapping("/api/users/conversations")
@RequiredArgsConstructor
@Slf4j
public class ConversationController {

    private static final Logger logger = LoggerFactory.getLogger(ConversationController.class);

    private final ConversationService conversationService;

    private final AuthenticationHelper authHelper;

    private final ConversationRepository conversationRepository;

    @GetMapping
    public ResponseEntity<?> getUserConversations() {
        try {
            String userId = authHelper.getAuthenticatedUserId();
            List<ConversationDto> conversations = conversationService.getUserConversations(userId);
            return ResponseEntity.ok(conversations);
        } catch (ApiException e) {
            logger.warn("API Exception: {}", e.getMessage());
            return ResponseEntity.status(e.getStatus()).body(e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error", e);
            return ResponseEntity.internalServerError().body("Error processing request");
        }
    }

    @PostMapping
    public ResponseEntity<?> createConversation(@RequestBody CreateConversationRequest request) {
        try {
            String userId = authHelper.getAuthenticatedUserId();

            // Check if conversation already exists
            Optional<Conversation> existing = conversationRepository.findByParticipantIdAndUserAdId(userId, request.getItemId());

            if (existing.isPresent()) {
                log.info("Existing conversation found for userId={} and itemId={}", userId, request.getItemId());
                ConversationDto existingDto = conversationService.convertToSecureDto(existing.get());
                return ResponseEntity.ok(existingDto); // 200 OK for existing
            }

            // Else, create new conversation
            ConversationDto conversation = conversationService.createConversation(userId, request.getItemId());
            return ResponseEntity.status(HttpStatus.CREATED).body(conversation); // 201 Created

        } catch (ApiException e) {
            logger.warn("API Exception while creating conversation: {}", e.getMessage());
            return ResponseEntity.status(e.getStatus()).body(e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error creating conversation", e);
            return ResponseEntity.internalServerError().body("Error processing request");
        }
    }

    @DeleteMapping("/{conversationId}")
    public ResponseEntity<?> deleteConversation(@PathVariable String conversationId) {
        String userId = authHelper.getAuthenticatedUserId();

        Optional<Conversation> optionalConversation = conversationRepository.findById(conversationId);

        if (optionalConversation.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Conversation not found with ID: " + conversationId);
        }

        Conversation conversation = optionalConversation.get();

        // Check if the user is allowed to delete the conversation
        boolean isAuthorized = conversation.getParticipant().getId().equals(userId) ||
                conversation.getSeller().getId().equals(userId);

        if (!isAuthorized) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("You are not authorized to delete this conversation.");
        }

        conversationRepository.deleteById(conversationId);
        return ResponseEntity.ok().body("Conversation deleted successfully.");
    }

    @Data
    public static class CreateConversationRequest {
        private String participantId;
        private String itemId;
    }

    @Data
    public static class ConversationDto {
        private String id;
        private UserDto participant; // Only shows buyer info
        private ItemDto item;
        private String lastMessage;
        private LocalDateTime timestamp;
        private int unread;
        private String status;
        // No seller field exposed
    }

    @Data
    public static class UserDto {
        private String name;  // No ID exposed
        private String avatar;
        private boolean verified;
        private double rating;
    }

    @Data
    public static class ItemDto {
        private String id;
        private String title;
        private double price;
        private String image;
        // No owner/seller reference
    }
}