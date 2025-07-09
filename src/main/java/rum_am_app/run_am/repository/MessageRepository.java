package rum_am_app.run_am.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import rum_am_app.run_am.model.Message;

import java.util.List;

@Repository
public interface MessageRepository extends MongoRepository<Message, String> {
    List<Message> findByConversationIdOrderByTimestampAsc(String conversationId);

    @Query(value = "{'conversationId': ?0}", sort = "{'timestamp': -1}")
    List<Message> findLatestMessages(String conversationId, Pageable pageable);
}
