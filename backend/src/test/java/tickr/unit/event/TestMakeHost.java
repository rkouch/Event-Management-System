package tickr.unit.event;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.eclipse.jetty.server.Authentication.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jdk.jfr.Event;
import tickr.CreateEventReqBuilder;
import tickr.TestHelper;
import tickr.application.TickrController;
import tickr.application.serialised.requests.CreateEventRequest;
import tickr.application.serialised.requests.EditHostRequest;
import tickr.application.serialised.requests.UserRegisterRequest;
import tickr.persistence.DataModel;
import tickr.persistence.HibernateModel;
import tickr.persistence.ModelSession;
import tickr.server.exceptions.BadRequestException;
import tickr.server.exceptions.ForbiddenException;

public class TestMakeHost {
    private DataModel model;
    private TickrController controller;
    private ModelSession session;

    private String eventId;
    private String authToken; 
    private String newHostAuthToken;
    private String newHostEmail = "newhost@example.com";
    
    @BeforeEach
    public void setup () {
        model = new HibernateModel("hibernate-test.cfg.xml");
        controller = new TickrController();

        List<CreateEventRequest.SeatingDetails> seatingDetails = new ArrayList<>();
        seatingDetails.add(new CreateEventRequest.SeatingDetails("SectionA", 10, 50, true));
        seatingDetails.add(new CreateEventRequest.SeatingDetails("SectionB", 20, 30, true));

        session = model.makeSession();

        authToken = controller.userRegister(session,
        new UserRegisterRequest("test", "first", "last", "test1@example.com",
                "Password123!", "2022-04-14")).authToken;

        newHostAuthToken = controller.userRegister(session,
        new UserRegisterRequest("test", "first", "last", "newhost@example.com",
                "Password123!", "2022-04-14")).authToken;
        var newHostId =  controller.userSearch(session, Map.of("email", newHostEmail)).userId;
        
        eventId = controller.createEvent(session, new CreateEventReqBuilder()
            .withEventName("Test Event")
            .withSeatingDetails(seatingDetails)
            .withStartDate(LocalDateTime.now().plusDays(1))
            .withEndDate(LocalDateTime.now().plusDays(2))
            .withAdmins(Set.of(newHostId))
            .build(authToken)).event_id;
            
        
        session = TestHelper.commitMakeSession(model, session);

    }

    @AfterEach
    public void cleanup () {
        model.cleanup();
    }

    @Test
    public void testExceptions() {
        assertThrows(ForbiddenException.class, () -> controller.makeHost(session, new EditHostRequest(authToken, eventId, "invalidemail@email.com")));
        assertThrows(ForbiddenException.class, () -> controller.makeHost(session, new EditHostRequest(authToken, UUID.randomUUID().toString(), newHostEmail)));
        controller.userRegister(session,
        new UserRegisterRequest("test", "first", "last", "randomuser@email.com",
                "Password123!", "2022-04-14"));
        session = TestHelper.commitMakeSession(model, session);
        // assertThrows(BadRequestException.class, () -> controller.makeHost(session, new EditHostRequest(authToken, UUID.randomUUID().toString(), "randomuser@email.com")));
    }

    @Test 
    public void testMakeHost() {
        controller.makeHost(session, new EditHostRequest(authToken, eventId, newHostEmail));
        session = TestHelper.commitMakeSession(model, session);
        var response = controller.eventView(session, Map.of("event_id", eventId, "auth_token", authToken));
        String newHostId = controller.userSearch(session, Map.of("email", newHostEmail)).userId;
        assertEquals(newHostId, response.host_id);
        assertFalse(response.admins.contains(newHostId));
        assertTrue(response.admins.contains(controller.authenticateToken(session, authToken).getId().toString()));
        // var newHost = session.getByUnique(User.class, "email", newHostEmail).orElse(null);
        // Event event = session.getById(Event.class, UUID.fromString(eventId)).orElse(null);
        // assertEquals(newHost, event.getHost());
        
    }

}
