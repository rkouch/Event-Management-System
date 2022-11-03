package tickr.unit.event;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
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
import tickr.application.serialised.requests.EventDeleteRequest;
import tickr.application.serialised.requests.UserRegisterRequest;
import tickr.application.serialised.responses.EventHostingsResponse;
import tickr.persistence.DataModel;
import tickr.persistence.HibernateModel;
import tickr.persistence.ModelSession;
import tickr.server.exceptions.BadRequestException;
import tickr.server.exceptions.ForbiddenException;
import tickr.server.exceptions.UnauthorizedException;

public class TestEventHostingsPastFuture {
    private DataModel model;
    private TickrController controller;
    private ModelSession session;

    private String eventId;
    private String authToken; 
    List<CreateEventRequest.SeatingDetails> seatingDetails;
    private String email = "test1@example.com";

    private int maxEvents = 16; 
    
    @BeforeEach
    public void setup () {
        model = new HibernateModel("hibernate-test.cfg.xml");
        controller = new TickrController();

        seatingDetails = new ArrayList<>();
        seatingDetails.add(new CreateEventRequest.SeatingDetails("SectionA", 10, 50, true));
        seatingDetails.add(new CreateEventRequest.SeatingDetails("SectionB", 20, 30, true));

        session = model.makeSession();

        authToken = controller.userRegister(session,
        new UserRegisterRequest("test", "first", "last", "test1@example.com",
                "Password123!", "2022-04-14")).authToken;
        
        controller.createEventUnsafe(session, new CreateEventReqBuilder()
                .withEventName("Test Event")
                .withSeatingDetails(seatingDetails)
                .withStartDate(LocalDateTime.now().minusDays(5))
                .withEndDate(LocalDateTime.now().minusDays(3))
                .build(authToken));
        session = TestHelper.commitMakeSession(model, session);

        controller.createEventUnsafe(session, new CreateEventReqBuilder()
                .withEventName("Test Event")
                .withSeatingDetails(seatingDetails)
                .withStartDate(LocalDateTime.now().minusDays(2))
                .withEndDate(LocalDateTime.now().minusDays(1))
                .build(authToken));
        session = TestHelper.commitMakeSession(model, session);

        controller.createEventUnsafe(session, new CreateEventReqBuilder()
                .withEventName("Test Event")
                .withSeatingDetails(seatingDetails)
                .withStartDate(LocalDateTime.now().plusDays(1))
                .withEndDate(LocalDateTime.now().plusDays(2))
                .build(authToken));
        session = TestHelper.commitMakeSession(model, session);
        
        controller.createEventUnsafe(session, new CreateEventReqBuilder()
                .withEventName("Test Event")
                .withSeatingDetails(seatingDetails)
                .withStartDate(LocalDateTime.now().plusDays(1))
                .withEndDate(LocalDateTime.now().plusDays(2))
                .build(authToken));
        session = TestHelper.commitMakeSession(model, session);

        controller.createEventUnsafe(session, new CreateEventReqBuilder()
                .withEventName("Test Event")
                .withSeatingDetails(seatingDetails)
                .withStartDate(LocalDateTime.now().plusDays(1))
                .withEndDate(LocalDateTime.now().plusDays(2))
                .build(authToken));
        session = TestHelper.commitMakeSession(model, session);
    }

    @AfterEach
    public void cleanup () {
        model.cleanup();
    }

    @Test 
    public void testFutureHostings() {
        int pageStart = 0;
        int maxResults = 10;
        var events = controller.eventHostingFuture(session, Map.of(
            "email", email, 
            "page_start", Integer.toString(pageStart), 
            "max_results", Integer.toString(maxResults)
        ));
        assertEquals(3, events.eventIds.size());
        assertEquals(3, events.numResults);
    }

    @Test 
    public void testPastHostings() {
        int pageStart = 0;
        int maxResults = 10;
        var events = controller.eventHostingPast(session, Map.of(
            "email", email, 
            "page_start", Integer.toString(pageStart), 
            "max_results", Integer.toString(maxResults)
        ));
        assertEquals(2, events.eventIds.size());
        assertEquals(2, events.numResults);
    }

    @Test 
    public void testExceptions() {
        int pageStart = 0;
        int maxResults = 10;
        assertThrows(BadRequestException.class, () -> controller.eventHostingPast(session, Map.of(
            "page_start", Integer.toString(pageStart), 
            "max_results", Integer.toString(maxResults)
        )));
        assertThrows(BadRequestException.class, () -> controller.eventHostingPast(session, Map.of(
            "email", email, 
            "max_results", Integer.toString(maxResults)
        )));
        assertThrows(BadRequestException.class, () -> controller.eventHostingPast(session, Map.of(
            "email", email, 
            "page_start", Integer.toString(pageStart)
        )));
        assertThrows(ForbiddenException.class, () -> controller.eventHostingPast(session, Map.of(
            "email", UUID.randomUUID().toString(), 
            "page_start", Integer.toString(pageStart), 
            "max_results", Integer.toString(maxResults)
        )));
        assertThrows(BadRequestException.class, () -> controller.eventHostingPast(session, Map.of(
            "email", email, 
            "page_start", Integer.toString(-1), 
            "max_results", Integer.toString(maxResults)
        )));
        assertThrows(BadRequestException.class, () -> controller.eventHostingPast(session, Map.of(
            "email", email, 
            "page_start", Integer.toString(pageStart), 
            "max_results", Integer.toString(-1)
        )));
        assertThrows(BadRequestException.class, () -> controller.eventHostingFuture(session, Map.of(
            "page_start", Integer.toString(pageStart), 
            "max_results", Integer.toString(maxResults)
        )));
        assertThrows(BadRequestException.class, () -> controller.eventHostingFuture(session, Map.of(
            "email", email, 
            "max_results", Integer.toString(maxResults)
        )));
        assertThrows(BadRequestException.class, () -> controller.eventHostingFuture(session, Map.of(
            "email", email, 
            "page_start", Integer.toString(pageStart)
        )));
        assertThrows(ForbiddenException.class, () -> controller.eventHostingFuture(session, Map.of(
            "email", UUID.randomUUID().toString(), 
            "page_start", Integer.toString(pageStart), 
            "max_results", Integer.toString(maxResults)
        )));
        assertThrows(BadRequestException.class, () -> controller.eventHostingFuture(session, Map.of(
            "email", email, 
            "page_start", Integer.toString(-1), 
            "max_results", Integer.toString(maxResults)
        )));
        assertThrows(BadRequestException.class, () -> controller.eventHostingFuture(session, Map.of(
            "email", email, 
            "page_start", Integer.toString(pageStart), 
            "max_results", Integer.toString(-1)
        )));
    }
}
