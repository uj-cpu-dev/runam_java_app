package rum_am_app.run_am.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.AuthorizationServiceException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rum_am_app.run_am.dtorequest.MessageRequest;
import rum_am_app.run_am.exception.ApiException;
import rum_am_app.run_am.model.Conversation;
import rum_am_app.run_am.model.Item;
import rum_am_app.run_am.model.Message;
import rum_am_app.run_am.model.User;
import rum_am_app.run_am.repository.ConversationRepository;
import rum_am_app.run_am.repository.ItemRepository;
import rum_am_app.run_am.repository.MessageRepository;
import rum_am_app.run_am.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public List<Conversation> getConversations(String userId, String search) {
        if (search != null && !search.isEmpty()) {
            return conversationRepository.searchConversations(search);
        }
        return conversationRepository.findByParticipantId(userId);
    }

    public List<Message> getMessages(String conversationId) {
        return messageRepository.findByConversationIdOrderByTimestampAsc(conversationId);
    }

    @Transactional
    public Message sendMessage(String conversationId, String senderId, MessageRequest request) {
        // Verify conversation exists
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ApiException("CONVERSATION not found", HttpStatus.BAD_REQUEST, "CONVERSATION_NOT_FOUND"));

        // Verify sender is participant
        if (!conversation.getParticipant().getId().equals(senderId)) {
            throw new AuthorizationServiceException("Not a participant in this conversation");
        }

        // Create and save message
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new ApiException("User not found", HttpStatus.BAD_REQUEST, "USER_NOT_FOUND"));

        Message message = new Message();
        message.setConversationId(conversationId);
        message.setSender(sender);
        message.setContent(request.getContent());
        message.setTimestamp(LocalDateTime.now());
        message.setStatus("sent");
        Message savedMessage = messageRepository.save(message);

        // Update conversation
        conversation.setLastMessage(request.getContent());
        conversation.setTimestamp(LocalDateTime.now());
        conversation.setUnread(0);
        conversationRepository.save(conversation);

        // Send real-time update
        messagingTemplate.convertAndSend(
                "/topic/messages/" + conversationId,
                new MessageRequest.MessageDto(savedMessage)
        );

        return savedMessage;
    }

    public Conversation createConversation(String participantId, String itemId) {
        User participant = userRepository.findById(participantId)
                .orElseThrow(() -> new ApiException("User not found", HttpStatus.BAD_REQUEST, "USER_NOT_FOUND"));

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ApiException("Item not found", HttpStatus.BAD_REQUEST, "ITEM_NOT_FOUND"));

        Conversation conversation = new Conversation();
        conversation.setParticipant(participant);
        conversation.setItem(item);
        conversation.setLastMessage("");
        conversation.setTimestamp(LocalDateTime.now());
        conversation.setUnread(0);
        conversation.setStatus("active");

        return conversationRepository.save(conversation);
    }
}
