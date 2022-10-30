package tickr.unit.event;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
public class TestUserEvents {
    private DataModel model;
    private TickrController controller;
    private ModelSession session;

    private String authToken; 

    private List<CreateEventRequest.SeatingDetails> seatingDetails;
    
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
        
        controller.createEvent(session, new CreateEventReqBuilder()
            .withEventName("Test Event")
            .withSeatingDetails(seatingDetails)
            .withStartDate(LocalDateTime.now().plusDays(1))
            .withEndDate(LocalDateTime.now().plusDays(2))
            .build(authToken));
        session = TestHelper.commitMakeSession(model, session);

        controller.createEvent(session, new CreateEventReqBuilder()
            .withEventName("Test Event")
            .withSeatingDetails(seatingDetails)
            .withStartDate(LocalDateTime.now().plusDays(3))
            .withEndDate(LocalDateTime.now().plusDays(4))
            .build(authToken));
        session = TestHelper.commitMakeSession(model, session);

        controller.createEvent(session, new CreateEventReqBuilder()
            .withEventName("Test Event")
            .withSeatingDetails(seatingDetails)
            .withStartDate(LocalDateTime.now().plusDays(5))
            .withEndDate(LocalDateTime.now().plusDays(6))
            .build(authToken));
        session = TestHelper.commitMakeSession(model, session);

        controller.createEvent(session, new CreateEventReqBuilder()
            .withEventName("Test Event")
            .withSeatingDetails(seatingDetails)
            .withStartDate(LocalDateTime.now().plusDays(15))
            .withEndDate(LocalDateTime.now().plusDays(16))
            .build(authToken));
        session = TestHelper.commitMakeSession(model, session);

        controller.createEvent(session, new CreateEventReqBuilder()
            .withEventName("Test Event")
            .withSeatingDetails(seatingDetails)
            .withStartDate(LocalDateTime.now().plusDays(20))
            .withEndDate(LocalDateTime.now().plusDays(21))
            .build(authToken));
        session = TestHelper.commitMakeSession(model, session);
        
    }

    @AfterEach
    public void cleanup () {
        model.cleanup();
    }

    @Test 
    public void testGetUserEvents () {
        var events = controller.userEvents(session, Map.of(
            "page_start", Integer.toString(0), 
            "max_results", Integer.toString(5), 
            "before", LocalDateTime.now().plusDays(21).format(DateTimeFormatter.ISO_DATE_TIME)
        )).getEventIds();
        assertEquals(5, events.size());

        events = controller.userEvents(session, Map.of(
            "page_start", Integer.toString(0), 
            "max_results", Integer.toString(5), 
            "before", LocalDateTime.now().plusDays(17).format(DateTimeFormatter.ISO_DATE_TIME)
        )).getEventIds();
        assertEquals(4, events.size());

        events = controller.userEvents(session, Map.of(
            "page_start", Integer.toString(0), 
            "max_results", Integer.toString(5), 
            "before", LocalDateTime.now().plusDays(14).format(DateTimeFormatter.ISO_DATE_TIME)
        )).getEventIds();
        assertEquals(3, events.size());

        events = controller.userEvents(session, Map.of(
            "page_start", Integer.toString(0), 
            "max_results", Integer.toString(5), 
            "before", LocalDateTime.now().plusMinutes(10).format(DateTimeFormatter.ISO_DATE_TIME)
        )).getEventIds();
        assertEquals(0, events.size());
    }

    @Test
    public void testUserEventsPagination() {
        int pageStart = 0;
        int maxResults = 2;
        int numEvents = 5; 

        var list1 = controller.userEvents(session, Map.of(
            "page_start", Integer.toString(pageStart), 
            "max_results", Integer.toString(numEvents),
            "before", LocalDateTime.now().plusDays(21).format(DateTimeFormatter.ISO_DATE_TIME)
        )).getEventIds();

        List<String> list2 = new ArrayList<>(); 
        for (int i = -1; i < numEvents / maxResults; i++) {
            var list = controller.userEvents(session, Map.of(
                "page_start", Integer.toString(pageStart), 
                "max_results", Integer.toString(maxResults),
                "before", LocalDateTime.now().plusDays(21).format(DateTimeFormatter.ISO_DATE_TIME)
            ));
            session = TestHelper.commitMakeSession(model, session);
            if (i != 1) {
                assertEquals(maxResults, list.eventIds.size());
            } else {
                assertEquals(maxResults - 1, list.eventIds.size());
            }
            assertEquals(numEvents, list.numResults);
            pageStart += maxResults;
            list2.addAll(list.eventIds);
        }
        assertEquals(list1, list2);
    }

    @Test 
    public void testExceptions () {
        assertThrows(BadRequestException.class, () -> controller.userEvents(session, Map.of(          
            "max_results", Integer.toString(5), 
            "before", LocalDateTime.now().plusDays(0).format(DateTimeFormatter.ISO_DATE_TIME)
        )));
        assertThrows(BadRequestException.class, () -> controller.userEvents(session, Map.of(
            "page_start", Integer.toString(0), 
            "before", LocalDateTime.now().plusDays(0).format(DateTimeFormatter.ISO_DATE_TIME)
        )));
        assertThrows(BadRequestException.class, () -> controller.userEvents(session, Map.of(
            "page_start", Integer.toString(-1), 
            "max_results", Integer.toString(5), 
            "before", LocalDateTime.now().plusDays(0).format(DateTimeFormatter.ISO_DATE_TIME)
        )));
        assertThrows(BadRequestException.class, () -> controller.userEvents(session, Map.of(
            "page_start", Integer.toString(0), 
            "max_results", Integer.toString(-1), 
            "before", LocalDateTime.now().plusDays(0).format(DateTimeFormatter.ISO_DATE_TIME)
        )));
        assertThrows(BadRequestException.class, () -> controller.userEvents(session, Map.of(
            "page_start", Integer.toString(0), 
            "max_results", Integer.toString(5), 
            "before", "21-01-2001"
        )));
        assertThrows(ForbiddenException.class, () -> controller.userEvents(session, Map.of(
            "page_start", Integer.toString(0), 
            "max_results", Integer.toString(5), 
            "before", LocalDateTime.now().minusDays(5).format(DateTimeFormatter.ISO_DATE_TIME)
        )));
    }
}