package co.com.scl.repository.strategy;

import co.com.scl.config.SecurityProperties;
import com.mongodb.client.MongoClient;
import org.bson.Document;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * MongoUserAuthStrategy's interesting logic (dot-notation field lookup and role
 * parsing) lives in private helper methods. Exercising findByUsername end-to-end
 * would require a live MongoDB server, so these helpers are invoked via reflection
 * against an instance whose client construction never opens a network connection.
 */
class MongoUserAuthStrategyTest {

    private MongoUserAuthStrategy strategy;

    @BeforeEach
    void setUp() {
        SecurityProperties properties = new SecurityProperties();
        properties.getDatasource().setUrl("mongodb://localhost:27017");
        properties.getDatasource().setDatabase("test");
        strategy = new MongoUserAuthStrategy(properties);
    }

    @AfterEach
    void closeClient() throws Exception {
        Field field = MongoUserAuthStrategy.class.getDeclaredField("mongoClient");
        field.setAccessible(true);
        ((MongoClient) field.get(strategy)).close();
    }

    private Object invokePrivate(String methodName, Class<?>[] paramTypes, Object... args) throws Exception {
        Method method = MongoUserAuthStrategy.class.getDeclaredMethod(methodName, paramTypes);
        method.setAccessible(true);
        return method.invoke(strategy, args);
    }

    @Test
    void getNestedStringResolvesTopLevelField() throws Exception {
        Document doc = new Document("username", "john.doe");

        Object result = invokePrivate("getNestedString", new Class[]{Document.class, String.class}, doc, "username");

        assertThat(result).isEqualTo("john.doe");
    }

    @Test
    void getNestedStringResolvesDottedPath() throws Exception {
        Document doc = new Document("authentication", new Document("username", "john.doe"));

        Object result = invokePrivate("getNestedString", new Class[]{Document.class, String.class},
            doc, "authentication.username");

        assertThat(result).isEqualTo("john.doe");
    }

    @Test
    void getNestedStringReturnsNullWhenIntermediateNodeIsMissing() throws Exception {
        Document doc = new Document("username", "john.doe");

        Object result = invokePrivate("getNestedString", new Class[]{Document.class, String.class},
            doc, "authentication.username");

        assertThat(result).isNull();
    }

    @Test
    void getNestedBooleanResolvesDottedPath() throws Exception {
        Document doc = new Document("account", new Document("active", true));

        Object result = invokePrivate("getNestedBoolean", new Class[]{Document.class, String.class},
            doc, "account.active");

        assertThat(result).isEqualTo(true);
    }

    @Test
    void getRolesParsesRolesFromAList() throws Exception {
        Document doc = new Document("roles", List.of("admin", " user ", ""));

        @SuppressWarnings("unchecked")
        Set<String> roles = (Set<String>) invokePrivate("getRoles",
            new Class[]{Document.class, String.class, String.class}, doc, "roles", ",");

        assertThat(roles).containsExactly("admin", "user");
    }

    @Test
    void getRolesParsesRolesFromADelimitedString() throws Exception {
        Document doc = new Document("roles", "admin, user , ops");

        @SuppressWarnings("unchecked")
        Set<String> roles = (Set<String>) invokePrivate("getRoles",
            new Class[]{Document.class, String.class, String.class}, doc, "roles", ",");

        assertThat(roles).containsExactly("admin", "user", "ops");
    }

    @Test
    void getRolesReturnsEmptySetWhenFieldIsMissing() throws Exception {
        Document doc = new Document();

        @SuppressWarnings("unchecked")
        Set<String> roles = (Set<String>) invokePrivate("getRoles",
            new Class[]{Document.class, String.class, String.class}, doc, "roles", ",");

        assertThat(roles).isEmpty();
    }

    @Test
    void getRolesHonorsACustomSeparator() throws Exception {
        Document doc = new Document("roles", "admin|user");

        @SuppressWarnings("unchecked")
        Set<String> roles = (Set<String>) invokePrivate("getRoles",
            new Class[]{Document.class, String.class, String.class}, doc, "roles", "|");

        assertThat(roles).containsExactly("admin", "user");
    }
}
