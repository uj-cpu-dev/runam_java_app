package rum_am_app.run_am.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import rum_am_app.run_am.model.Conversation;

import java.util.List;

@Repository
public interface ConversationRepository extends MongoRepository<Conversation, String> {
    List<Conversation> findByParticipantId(String participantId);

    @Query("{'$or': ["
            + "{'participant.name': {$regex: ?0, $options: 'i'}}, "
            + "{'item.title': {$regex: ?0, $options: 'i'}}"
            + "]}")
    List<Conversation> searchConversations(String searchTerm);
}
