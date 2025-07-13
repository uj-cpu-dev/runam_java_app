package rum_am_app.run_am.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import rum_am_app.run_am.model.Message;
import rum_am_app.run_am.util.MessageRepositoryCustom;

import java.util.List;

@Repository
public interface MessageRepository extends MongoRepository<Message, String>, MessageRepositoryCustom {

    List<Message> findByConversationIdOrderByTimestampAsc(String conversationId);
}
