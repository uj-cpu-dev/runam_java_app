package rum_am_app.run_am.repository;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.repository.MongoRepository;
import rum_am_app.run_am.model.Item;

public interface ItemRepository extends MongoRepository<Item, String> {

}
