package tickr.integration;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

import spark.Spark;
import tickr.application.entities.TestEntity;
import tickr.application.serialised.responses.TestResponses;
import tickr.persistence.HibernateModel;
import tickr.server.Server;

import java.util.Map;
import java.util.UUID;

public class TestTestEntity {
    HibernateModel hibernateModel;
    HTTPHelper httpHelper;
    String serverUrl;

    @BeforeEach
    public void setup () {
        hibernateModel = new HibernateModel("hibernate-test.cfg.xml");

        Server.start(8080, null, hibernateModel);
        httpHelper = new HTTPHelper("http://localhost:8080");
        Spark.awaitInitialization();
    }
    @AfterEach
    public void finish () {
        Spark.stop();
        hibernateModel.cleanup();
        Spark.awaitStop();
    }

    @Test
    public void testGetNoEntities () {
        var response = httpHelper.get("/api/test/get/all");
        assertEquals(200, response.getStatus());
        assertTrue(response.getBody().isJsonObject());

        var obj = response.getBody().getAsJsonObject();
        assertTrue(obj.has("entities"));
        assertTrue(obj.get("entities").isJsonArray());
        var arr = obj.getAsJsonArray("entities");
        assertTrue(arr.isEmpty());
    }

    @Test
    public void testPost () {
        var response = httpHelper.post("/api/test/post", new TestResponses.PostRequest("John Doe", "test@example.com"));
        assertEquals(200, response.getStatus());

        var result = response.getBody(TestEntity.class);

        assertEquals(result.getName(), "John Doe");
        assertEquals(result.getEmail(), "test@example.com");

        var response2 = httpHelper.get("/api/test/get/all");
        assertEquals(200, response2.getStatus());

        var entitiesList = response2.getBody(TestResponses.GetAll.class).entities;
        assertEquals(1, entitiesList.size());
        assertEquals(result, entitiesList.get(0));
    }

    @Test
    public void testGetEntity () {
        var response = httpHelper.post("/api/test/post", new TestResponses.PostRequest("John Doe", "test@example.com"));
        assertEquals(200, response.getStatus());
        var result = response.getBody(TestEntity.class);

        response = httpHelper.get("/api/test/get", Map.of("id", result.getId().toString()));
        assertEquals(200, response.getStatus());
        var result2 = response.getBody(TestEntity.class);

        assertEquals(result, result2);
    }

    @Test
    public void testPut () {
        var response = httpHelper.post("/api/test/post", new TestResponses.PostRequest("John Doe", "test@example.com"));
        assertEquals(200, response.getStatus());
        var result = response.getBody(TestEntity.class);

        response = httpHelper.put("/api/test/put", new TestResponses.PutRequest(result.getId(), "Jane Doe", "test2@example.com"));
        assertEquals(200, response.getStatus());
        var result2 = response.getBody(TestEntity.class);

        assertEquals("Jane Doe", result2.getName());
        assertEquals("test2@example.com", result2.getEmail());
        assertEquals(result.getId(), result2.getId());

        response = httpHelper.get("/api/test/get", Map.of("id", result2.getId().toString()));
        assertEquals(200, response.getStatus());
        var result3 = response.getBody(TestEntity.class);
        assertNotEquals(result, result3);
        assertEquals(result2, result3);
    }

    @Test
    public void testUniqueId () {
        var response = httpHelper.post("/api/test/post", new TestResponses.PostRequest("John Doe", "test@example.com"));
        assertEquals(200, response.getStatus());
        var result = response.getBody(TestEntity.class);

        response = httpHelper.post("/api/test/post", new TestResponses.PostRequest("Jane Doe", "test2@example.com"));
        assertEquals(200, response.getStatus());
        var result2 = response.getBody(TestEntity.class);

        assertNotEquals(result.getId(), result2.getId());
    }

    @Test
    public void testDelete () {
        var response = httpHelper.post("/api/test/post", new TestResponses.PostRequest("John Doe", "test@example.com"));
        assertEquals(200, response.getStatus());
        var result = response.getBody(TestEntity.class);

        response = httpHelper.delete("/api/test/delete", new TestResponses.DeleteRequest(result.getId()));
        assertEquals(200, response.getStatus());

        response = httpHelper.get("/api/test/get/all");
        assertEquals(200, response.getStatus());
        assertEquals(0, response.getBody(TestResponses.GetAll.class).entities.size());

        response = httpHelper.get("/api/test/get", Map.of("id", result.getId().toString()));
        assertEquals(404, response.getStatus());
    }

    @Test
    public void testErrors () {
        assertEquals(400, httpHelper.get("/api/test/get").getStatus());
        assertEquals(404, httpHelper.get("/api/test/get", Map.of("id", UUID.randomUUID().toString())).getStatus());

        assertEquals(400, httpHelper.post("/api/test/post", new TestResponses.PostRequest(null, null)).getStatus());
        assertEquals(400, httpHelper.put("/api/test/put", new TestResponses.PutRequest(UUID.randomUUID(), null, null)).getStatus());
        assertEquals(404, httpHelper.put("/api/test/put", new TestResponses.PutRequest(UUID.randomUUID(), "Jane Doe", "test2@example.com")).getStatus());
        assertEquals(404, httpHelper.delete("/api/test/delete", new TestResponses.DeleteRequest(UUID.randomUUID())).getStatus());
    }
}
