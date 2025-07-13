package rum_am_app.run_am.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rum_am_app.run_am.controller.MessageController;
import rum_am_app.run_am.exception.ApiException;
import rum_am_app.run_am.model.*;
import rum_am_app.run_am.repository.ConversationRepository;
import rum_am_app.run_am.repository.MessageRepository;
import rum_am_app.run_am.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.*;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;
    private final ConversationRepository conversationRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Transactional
    public MessageController.MessageDto sendMessageHttp(String conversationId, String senderId, String content) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ApiException("Conversation not found", NOT_FOUND, "CONVERSATION_NOT_FOUND"));

        String participantId = conversation.getParticipant().getId();
        String sellerId = conversation.getSeller().getId();

        if (!participantId.equals(senderId) && !sellerId.equals(senderId)) {
            throw new ApiException("Access denied", FORBIDDEN, "ACCESS_DENIED");
        }

        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new ApiException("User not found", NOT_FOUND, "USER_NOT_FOUND"));

        // Determine the receiver
        String receiverId = senderId.equals(participantId) ? sellerId : participantId;

        Message message = new Message();
        message.setConversationId(conversationId);
        message.setSender(sender);
        message.setContent(content);
        message.setTimestamp(LocalDateTime.now());
        message.setStatus("delivered");
        message.setRead(false);
        Message savedMessage = messageRepository.save(message);

        // Update conversation with unread count for receiver
        conversation.setLastMessage(content);
        conversation.setTimestamp(LocalDateTime.now());

        // Increment unread count if the receiver is not the sender
        if (!senderId.equals(receiverId)) {
            conversation.setUnread(conversation.getUnread() + 1);
        }

        conversationRepository.save(conversation);

        // Send real-time update
        messagingTemplate.convertAndSend(
                "/topic/messages/" + conversationId,
                convertToDto(savedMessage)
        );

        return convertToDto(savedMessage);
    }

    @Transactional(readOnly = true)
    public List<MessageController.MessageDto> getMessages(String conversationId, String userId) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ApiException("Conversation not found", NOT_FOUND, "CONVERSATION_NOT_FOUND"));

        String participantId = conversation.getParticipant().getId();
        String sellerId = conversation.getSeller().getId();

        if (!participantId.equals(userId) && !sellerId.equals(userId)) {
            throw new ApiException("Access denied", FORBIDDEN, "ACCESS_DENIED");
        }

        return messageRepository.findByConversationIdOrderByTimestampAsc(conversationId)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public void markMessagesAsRead(String conversationId, String userId) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ApiException("Conversation not found", NOT_FOUND, "CONVERSATION_NOT_FOUND"));

        // Verify user has access to this conversation
        String participantId = conversation.getParticipant().getId();
        String sellerId = conversation.getSeller().getId();

        if (!participantId.equals(userId) && !sellerId.equals(userId)) {
            throw new ApiException("Access denied", FORBIDDEN, "ACCESS_DENIED");
        }

        // Mark all unread messages from other participants as read
        messageRepository.markMessagesAsRead(conversationId, userId);

        // Reset unread count for this conversation
        conversation.setUnread(0);
        conversationRepository.save(conversation);
    }

    private MessageController.MessageDto convertToDto(Message message) {
        MessageController.MessageDto dto = new MessageController.MessageDto();
        dto.setId(message.getId());
        dto.setConversationId(message.getConversationId());
        dto.setSender(convertToDto(message.getSender()));
        dto.setContent(message.getContent());
        dto.setTimestamp(message.getTimestamp());
        dto.setStatus(message.getStatus());
        return dto;
    }

    private MessageController.UserDto convertToDto(User user) {
        MessageController.UserDto dto = new MessageController.UserDto();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setAvatar(user.getAvatarUrl());
        dto.setVerified(user.isEmailVerified());
        dto.setRating(user.getRating());
        return dto;
    }
}