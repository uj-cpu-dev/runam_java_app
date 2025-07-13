package rum_am_app.run_am.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import rum_am_app.run_am.model.Conversation;

import java.util.List;
import java.util.Optional;
@Repository
public interface ConversationRepository extends MongoRepository<Conversation, String> {

    @Query("{ $or: [ "
            + "{ 'participant.id': ?0 }, "
            + "{ 'seller.id': ?0 } "
            + "] }")
    List<Conversation> findByParticipantIdOrSellerId(String participantId, String sellerId);

    Optional<Conversation> findByParticipantIdAndUserAdId(String participantId, String userAdId);
}
