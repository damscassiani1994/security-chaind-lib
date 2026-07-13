package co.com.scl.repository.strategy;

import co.com.scl.config.SecurityProperties;
import co.com.scl.model.UserAuthModel;
import co.com.scl.repository.IUserAuthRepository;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.util.Optional;

public class MongoUserAuthStrategy implements IUserAuthRepository {

    private final SecurityProperties properties;
    private final MongoClient mongoClient;

    public MongoUserAuthStrategy(SecurityProperties properties) {
        this.properties = properties;
        this.mongoClient = MongoClients.create(properties.getDatasource().getUrl());
    }

    @Override
    public Optional<UserAuthModel> findByUsername(String username) {
        SecurityProperties.UserProperties u = properties.getUser();
        MongoDatabase db = mongoClient.getDatabase(properties.getDatasource().getDatabase());
        MongoCollection<Document> collection = db.getCollection(u.getCollectionOrTable());

        // MongoDB natively supports dot-notation in queries (e.g. "authentication.username")
        Document result = collection.find(new Document(u.getUsernameField(), username)).first();

        if (result == null) {
            return Optional.empty();
        }

        return Optional.of(UserAuthModel.builder()
            .username(getNestedString(result, u.getUsernameField()))
            .password(getNestedString(result, u.getPasswordField()))
            .active(Boolean.TRUE.equals(getNestedBoolean(result, u.getIsActiveField())))
            .build());
    }

    private String getNestedString(Document doc, String path) {
        String[] parts = path.split("\\.", 2);
        if (parts.length == 1) return doc.getString(parts[0]);
        Document nested = doc.get(parts[0], Document.class);
        return nested != null ? getNestedString(nested, parts[1]) : null;
    }

    private Boolean getNestedBoolean(Document doc, String path) {
        String[] parts = path.split("\\.", 2);
        if (parts.length == 1) return doc.getBoolean(parts[0]);
        Document nested = doc.get(parts[0], Document.class);
        return nested != null ? getNestedBoolean(nested, parts[1]) : null;
    }
}