package rum_am_app.run_am.service;

import com.mongodb.DuplicateKeyException;
import com.mongodb.MongoWriteException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rum_am_app.run_am.controller.ConversationController;
import rum_am_app.run_am.exception.ApiException;
import rum_am_app.run_am.model.Conversation;
import rum_am_app.run_am.model.Item;
import rum_am_app.run_am.model.User;
import rum_am_app.run_am.model.UserAd;
import rum_am_app.run_am.repository.ConversationRepository;
import rum_am_app.run_am.repository.UserAdRepository;
import rum_am_app.run_am.repository.UserRepository;
import rum_am_app.run_am.util.AuthenticationHelper;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConversationService {

    private final ConversationRepository conversationRepository;
    private final UserRepository userRepository;
    private final AuthenticationHelper authHelper;
    private final UserAdRepository userAdRepository;

    @Transactional(readOnly = true)
    public List<ConversationController.ConversationDto> getUserConversations(String userId) {
        try {
            return conversationRepository.findByParticipantIdOrSellerId(userId, userId)
                    .stream()
                    .map(this::convertToSecureDto)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new ApiException("Failed to retrieve conversations", INTERNAL_SERVER_ERROR, "CONVERSATION_RETRIEVAL_ERROR");
        }
    }

    public ConversationController.ConversationDto createConversation(String userId, String itemId) {
        // Properly unwrap Optionals here
        User buyer = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException("USER_NOT_FOUND", HttpStatus.NOT_FOUND, "User not found"));

        UserAd item = userAdRepository.findById(itemId)
                .orElseThrow(() -> new ApiException("ITEM_NOT_FOUND", HttpStatus.NOT_FOUND, "Item not found"));

        User seller = userRepository.findById(item.getUserId())
                .orElseThrow(() -> new ApiException("SELLER_NOT_FOUND", HttpStatus.NOT_FOUND, "Seller not found"));

        return createNewSecureConversation(buyer, seller, item);
    }

    private ConversationController.ConversationDto createNewSecureConversation(User buyer, User seller, UserAd item) {
        Conversation conversation = new Conversation();
        conversation.setParticipant(buyer);
        conversation.setSeller(seller);
        conversation.setUserAd(item);

        conversation.setParticipantId(buyer.getId());
        conversation.setUserAdId(item.getId()); // or item.getUserAdId() depending on structure

        conversation.setLastMessage("");
        conversation.setTimestamp(LocalDateTime.now());
        conversation.setUnread(0);
        conversation.setStatus("active");

        Conversation saved = conversationRepository.save(conversation);
        return convertToSecureDto(saved);
     }


        public ConversationController.ConversationDto convertToSecureDto(Conversation conversation) {
        ConversationController.ConversationDto dto = new ConversationController.ConversationDto();
        dto.setId(conversation.getId());

        // Only include participant (buyer) info
        dto.setParticipant(convertToUserDto(conversation.getParticipant()));

        // Include item info without seller reference
        dto.setItem(convertUserAdToItemDto(conversation.getUserAd()));

        dto.setLastMessage(conversation.getLastMessage());
        dto.setTimestamp(conversation.getTimestamp());
        dto.setUnread(conversation.getUnread());
        dto.setStatus(conversation.getStatus());

        return dto;
    }

    private ConversationController.UserDto convertToUserDto(User user) {
        ConversationController.UserDto dto = new ConversationController.UserDto();
        // Never include user ID in DTO
        dto.setName(user.getName());
        dto.setAvatar(user.getAvatarUrl());
        dto.setVerified(user.isEmailVerified() && user.isPhoneVerified());
        dto.setRating(user.getRating());
        return dto;
    }

    private ConversationController.ItemDto convertUserAdToItemDto(UserAd userAd) {
        ConversationController.ItemDto dto = new ConversationController.ItemDto();
        dto.setId(userAd.getId());
        dto.setTitle(userAd.getTitle());
        dto.setPrice(userAd.getPrice());
        dto.setImage(userAd.getImages() != null && !userAd.getImages().isEmpty() ? userAd.getImages().get(0).getUrl() : null);
        return dto;
    }
}
