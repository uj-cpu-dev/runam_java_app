package rum_am_app.run_am.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Repository;
import rum_am_app.run_am.util.MessageRepositoryCustom;

@Repository
public class MessageRepositoryImpl implements MessageRepositoryCustom {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public void markMessagesAsRead(String conversationId, String userId) {
        Query query = new Query();
        query.addCriteria(Criteria.where("conversationId").is(conversationId)
                .and("read").is(false)
                .and("sender.id").ne(userId));

        Update update = new Update().set("read", true);

        mongoTemplate.updateMulti(query, update, Message.class);
    }
}
