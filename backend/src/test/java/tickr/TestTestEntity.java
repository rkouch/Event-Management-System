package tickr;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tickr.application.TickrController;
import tickr.application.entities.TestEntity;
import tickr.application.responses.TestResponses;
import tickr.mock.IMockGenerator;
import tickr.mock.MockModel;
import tickr.server.exceptions.BadRequestException;
import tickr.server.exceptions.NotFoundException;

import java.util.ArrayList;
import java.util.Map;

public class TestTestEntity {
    private TickrController controller;
    private MockModel mockModel;

    @BeforeEach
    public void setupController () {
        controller = new TickrController();

        mockModel = new MockModel();
        mockModel.addTable(TestEntity.class);
        mockModel.registerTableColumn(TestEntity.class, "id", TestEntity::getId);
        mockModel.addGeneratedColumn(TestEntity.class, TestEntity::setId, IMockGenerator.sequentialGenerator(i -> i + 1), 0);
    }

    @Test
    public void testGetNoEntities () {
        var session = mockModel.makeSession();
        assertEquals(controller.testGetAll(session).entities, new ArrayList<>());
    }

    @Test
    public void testPost () {
        var session = mockModel.makeSession();
        var result = controller.testPost(session, new TestResponses.PostRequest("John Doe", "test@example.com"));

        session = mockModel.commitRemakeSession(session);

        assertEquals(result.getName(), "John Doe");
        assertEquals(result.getEmail(), "test@example.com");

        var entitiesList = controller.testGetAll(session).entities;

        assertEquals(entitiesList.size(), 1);
        assertEquals(entitiesList.get(0), result);
    }

    @Test
    public void testGetEntity () {
        var session = mockModel.makeSession();
        var result = controller.testPost(session, new TestResponses.PostRequest("John Doe", "test@example.com"));

        session = mockModel.commitRemakeSession(session);

        var result2 = controller.testGet(session, Map.of("id", Integer.toString(result.getId())));

        assertEquals(result2, result);
    }

    @Test
    public void testPut () {
        var session = mockModel.makeSession();
        var result = controller.testPost(session, new TestResponses.PostRequest("John Doe", "test@example.com"));

        session = mockModel.commitRemakeSession(session);

        var result2 = controller.testPut(session, new TestResponses.PutRequest(result.getId(), "Jane Doe", "test2@example.com"));

        session = mockModel.commitRemakeSession(session);

        assertEquals(result2.getName(), "Jane Doe");
        assertEquals(result2.getEmail(), "test2@example.com");
        assertEquals(result2.getId(), result.getId());

        var result3 = controller.testGet(session, Map.of("id", Integer.toString(result.getId())));
        assertNotEquals(result3, result);
        assertEquals(result3, result2);
    }

    @Test
    public void testUniqueId () {
        var session = mockModel.makeSession();

        var result = controller.testPost(session, new TestResponses.PostRequest("John Doe", "test@example.com"));
        session = mockModel.commitRemakeSession(session);

        var result2 = controller.testPost(session, new TestResponses.PostRequest("Jane Doe", "test2@example.com"));
        session = mockModel.commitRemakeSession(session);

        assertNotEquals(result2.getId(), result.getId());
        assertEquals(controller.testGet(session, Map.of("id", Integer.toString(result.getId()))), result);
        assertEquals(controller.testGet(session, Map.of("id", Integer.toString(result2.getId()))), result2);
    }

    @Test
    public void testDelete () {
        var session = mockModel.makeSession();
        var result = controller.testPost(session, new TestResponses.PostRequest("John Doe", "test@example.com"));

        session = mockModel.commitRemakeSession(session);

        var finalSession = session; // To keep Java happy
        assertDoesNotThrow(() -> controller.testDelete(finalSession, new TestResponses.DeleteRequest(result.getId())));
        session = mockModel.commitRemakeSession(session);

        assertEquals(controller.testGetAll(session).entities.size(), 0);
        var finalSession1 = session; // To keep Java happy
        assertThrows(NotFoundException.class, () -> controller.testGet(finalSession1, Map.of("id", Integer.toString(result.getId()))));
    }

    @Test
    public void testExceptions () {
        var session = mockModel.makeSession();
        assertThrows(BadRequestException.class, () -> controller.testGet(session, Map.of()));
        assertThrows(NotFoundException.class, () -> controller.testGet(session, Map.of("id", Integer.toString(4))));

        assertThrows(BadRequestException.class, () -> controller.testPost(session, new TestResponses.PostRequest(null, null)));
        assertThrows(BadRequestException.class, () -> controller.testPut(session, new TestResponses.PutRequest(0, null, null)));
        assertThrows(NotFoundException.class, () -> controller.testPut(session, new TestResponses.PutRequest(4, "Jane Doe", "test2@example.com")));
        assertThrows(NotFoundException.class, () -> controller.testDelete(session, new TestResponses.DeleteRequest(4)));
    }

}
