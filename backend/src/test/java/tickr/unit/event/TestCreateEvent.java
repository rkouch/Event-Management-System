package tickr.unit.event;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.eclipse.jetty.server.Authentication.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import tickr.TestHelper;
import tickr.application.TickrController;
import tickr.application.entities.Event;
import tickr.application.entities.Tag;
import tickr.application.serialised.SerializedLocation;
import tickr.application.serialised.requests.CreateEventRequest;
import tickr.application.serialised.requests.UserRegisterRequest;
import tickr.persistence.DataModel;
import tickr.persistence.HibernateModel;
import tickr.server.exceptions.BadRequestException;
import tickr.server.exceptions.ForbiddenException;
import tickr.server.exceptions.UnauthorizedException;
import tickr.util.CryptoHelper;

public class TestCreateEvent {
    private DataModel model;
    private TickrController controller;

    @BeforeEach
    public void setup () {
        model = new HibernateModel("hibernate-test.cfg.xml");
        controller = new TickrController();
        
    }

    @AfterEach
    public void cleanup () {
        model.cleanup();
    }

    @Test
    public void testMissingParamaters () {
        var session = model.makeSession();
        var authTokenString = controller.userRegister(session,
        new UserRegisterRequest("test", "first", "last", "test1@example.com",
                "Password123!", "2022-04-14")).authToken;
        // session = TestHelper.commitMakeSession(model, session);
        CreateEventRequest.SeatingDetails seats1 = new CreateEventRequest.SeatingDetails("sectionA", 100);
        CreateEventRequest.SeatingDetails seats2 = new CreateEventRequest.SeatingDetails("sectionB", 50);
        List<CreateEventRequest.SeatingDetails> seats = new ArrayList<CreateEventRequest.SeatingDetails>();
        SerializedLocation location = new SerializedLocation("test street", 12, null, "2000", "NSW", "Aus", "", "");
        seats.add(seats1);
        seats.add(seats2);
        Set<String> admins = new HashSet<>();
        admins.add("test1@example.com");
        Set<String> categories = new HashSet<>();
        categories.add("testcategory");
        Set<String> tags = new HashSet<>();
        tags.add("testtags");
        assertThrows(UnauthorizedException.class, () -> controller.createEvent(session, new CreateEventRequest("authTokenString", "test event", 
            "test picture", location, "2011-12-03T10:15:30", "2011-12-04T10:15:30", "description", seats, admins, categories, tags)));
        assertThrows(BadRequestException.class, () -> controller.createEvent(session, new CreateEventRequest("", "test event", 
            "test picture", location, "2011-12-03T10:15:30", "2011-12-04T10:15:30", "description", seats, admins, categories, tags)));
        assertThrows(BadRequestException.class, () -> controller.createEvent(session, new CreateEventRequest(authTokenString, "", 
            "test picture", location, "2011-12-03T10:15:30", "2011-12-04T10:15:30", "description", seats, admins, categories, tags)));
        assertThrows(BadRequestException.class, () -> controller.createEvent(session, new CreateEventRequest(authTokenString, null, 
            "test picture", location, "2011-12-03T10:15:30", "2011-12-04T10:15:30", "description", seats, admins, categories, tags)));
        assertThrows(BadRequestException.class, () -> controller.createEvent(session, new CreateEventRequest(authTokenString, "", 
            "test picture", null, "2011-12-03T10:15:30", "2011-12-04T10:15:30", "description", seats, admins, categories, tags)));
        assertThrows(BadRequestException.class, () -> controller.createEvent(session, new CreateEventRequest(authTokenString, "", 
            "test picture", location, null, "2011-12-04T10:15:30", "description", seats, admins, categories, tags)));
        assertThrows(BadRequestException.class, () -> controller.createEvent(session, new CreateEventRequest(authTokenString, "", 
            "test picture", location, "2011-12-03T10:15:30", null, "description", seats, admins, categories, tags)));
        assertThrows(BadRequestException.class, () -> controller.createEvent(session, new CreateEventRequest(authTokenString, "", 
            "test picture", location, "2011-12-03T10:15:30", "2011-12-04T10:15:30", "description", seats, null, categories, tags)));
        assertThrows(BadRequestException.class, () -> controller.createEvent(session, new CreateEventRequest(authTokenString, "", 
            "test picture", location, "2011-12-03T10:15:30", "2011-12-04T10:15:30", "description", seats, admins, null, tags)));
        assertThrows(BadRequestException.class, () -> controller.createEvent(session, new CreateEventRequest(authTokenString, "", 
            "test picture", location, "2011-12-03T10:15:30", "2011-12-04T10:15:30", "description", seats, admins, categories, null)));
        CreateEventRequest.SeatingDetails invalidSeats1 = new CreateEventRequest.SeatingDetails("", 100);
        CreateEventRequest.SeatingDetails invalidSeats2 = new CreateEventRequest.SeatingDetails(null, 50);
        List<CreateEventRequest.SeatingDetails> invalidSeats = new ArrayList<CreateEventRequest.SeatingDetails>();
        assertThrows(BadRequestException.class, () -> controller.createEvent(session, new CreateEventRequest(authTokenString, "", 
            "test picture", location, "2011-12-03T10:15:30", "2011-12-04T10:15:30", "description", invalidSeats, admins, categories, tags)));
    }

    @Test 
    public void testMissingLocationParamaters () {
        var session = model.makeSession();
        var authTokenString = controller.userRegister(session,
        new UserRegisterRequest("test", "first", "last", "test1@example.com",
                "Password123!", "2022-04-14")).authToken;
        // session = TestHelper.commitMakeSession(model, session);
        CreateEventRequest.SeatingDetails seats1 = new CreateEventRequest.SeatingDetails("sectionA", 100);
        CreateEventRequest.SeatingDetails seats2 = new CreateEventRequest.SeatingDetails("sectionB", 50);
        List<CreateEventRequest.SeatingDetails> seats = new ArrayList<CreateEventRequest.SeatingDetails>();
        seats.add(seats1);
        seats.add(seats2);
        Set<String> admins = new HashSet<>();
        admins.add("test1@example.com");
        Set<String> categories = new HashSet<>();
        categories.add("testcategory");
        Set<String> tags = new HashSet<>();
        tags.add("testtags");
        SerializedLocation location1 = new SerializedLocation("test street", 12, null, null, "NSW", "Aus", "", "");
        SerializedLocation location2 = new SerializedLocation("test street", 12, null, "2000", null, "Aus", "", "");
        SerializedLocation location3 = new SerializedLocation("test street", 12, null, "2000", "NSW", null, "", "");
        SerializedLocation location4 = new SerializedLocation("test street", 12, null, "2000", "NSW", "Aus", null, "");
        SerializedLocation location5 = new SerializedLocation("test street", 12, null, "2000", "NSW", "Aus", "", null);
        assertThrows(BadRequestException.class, () -> controller.createEvent(session, new CreateEventRequest(authTokenString, "", 
            "test picture", location1, "2011-12-03T10:15:30", "2011-12-04T10:15:30", "description", seats, admins, categories, tags)));
        assertThrows(BadRequestException.class, () -> controller.createEvent(session, new CreateEventRequest(authTokenString, "", 
            "test picture", location2, "2011-12-03T10:15:30", "2011-12-04T10:15:30", "description", seats, admins, categories, tags)));
        assertThrows(BadRequestException.class, () -> controller.createEvent(session, new CreateEventRequest(authTokenString, "", 
            "test picture", location3, "2011-12-03T10:15:30", "2011-12-04T10:15:30", "description", seats, admins, categories, tags)));
        assertThrows(BadRequestException.class, () -> controller.createEvent(session, new CreateEventRequest(authTokenString, "", 
            "test picture", location4, "2011-12-03T10:15:30", "2011-12-04T10:15:30", "description", seats, admins, categories, tags)));
        assertThrows(BadRequestException.class, () -> controller.createEvent(session, new CreateEventRequest(authTokenString, "", 
            "test picture", location5, "2011-12-03T10:15:30", "2011-12-04T10:15:30", "description", seats, admins, categories, tags)));
    }

    @Test 
    public void testInvalidToken () {
        var session = model.makeSession();
        CreateEventRequest.SeatingDetails seats1 = new CreateEventRequest.SeatingDetails("sectionA", 100);
        CreateEventRequest.SeatingDetails seats2 = new CreateEventRequest.SeatingDetails("sectionB", 50);
        List<CreateEventRequest.SeatingDetails> seats = new ArrayList<CreateEventRequest.SeatingDetails>();
        SerializedLocation location = new SerializedLocation("test street", 12, null, "2000", "NSW", "Aus", "", "");
        seats.add(seats1);
        seats.add(seats2);
        Set<String> admins = new HashSet<>();
        admins.add("test1@example.com");
        Set<String> categories = new HashSet<>();
        categories.add("testcategory");
        Set<String> tags = new HashSet<>();
        tags.add("testtags");
        assertThrows(UnauthorizedException.class, () -> controller.createEvent(session, new CreateEventRequest("authTokenString", "test event", 
        "test picture", location
        , "2011-12-03T10:15:30", 
        "2011-12-04T10:15:30", "description", seats, admins, categories, tags)));
    }

    @Test 
    public void testInvalidDates () {
        var session = model.makeSession();
        var authTokenString = controller.userRegister(session,
        new UserRegisterRequest("test", "first", "last", "test1@example.com",
                "Password123!", "2022-04-14")).authToken;
        CreateEventRequest.SeatingDetails seats1 = new CreateEventRequest.SeatingDetails("sectionA", 100);
        CreateEventRequest.SeatingDetails seats2 = new CreateEventRequest.SeatingDetails("sectionB", 50);
        List<CreateEventRequest.SeatingDetails> seats = new ArrayList<CreateEventRequest.SeatingDetails>();
        SerializedLocation location = new SerializedLocation("test street", 12, null, "2000", "NSW", "Aus", "", "");
        seats.add(seats1);
        seats.add(seats2);
        Set<String> admins = new HashSet<>();
        admins.add("test1@example.com");
        Set<String> categories = new HashSet<>();
        categories.add("testcategory");
        Set<String> tags = new HashSet<>();
        tags.add("testtags");
        assertThrows(BadRequestException.class, () -> controller.createEvent(session, new CreateEventRequest(authTokenString, "test event", 
        "test picture", location
        , "2011-12-04T10:15:30", 
        "2011-12-03T10:15:30", "description", seats, admins, categories, tags)));

        assertThrows(ForbiddenException.class, () -> controller.createEvent(session, new CreateEventRequest(authTokenString, "test event", 
        "test picture", location
        , "2011-12-04 10:15:30", 
        "2011-12-03 10:15:30", "description", seats, admins, categories, tags)));
    }

    @Test
    public void testCreateEvent () {
        var session = model.makeSession();
        var authTokenString = controller.userRegister(session,
        new UserRegisterRequest("test", "first", "last", "test1@example.com",
                "Password123!", "2022-04-14")).authToken;
        CreateEventRequest.SeatingDetails seats1 = new CreateEventRequest.SeatingDetails("sectionA", 100);
        CreateEventRequest.SeatingDetails seats2 = new CreateEventRequest.SeatingDetails("sectionB", 50);
        List<CreateEventRequest.SeatingDetails> seats = new ArrayList<CreateEventRequest.SeatingDetails>();
        SerializedLocation location = new SerializedLocation("test street", 12, null, "2000", "NSW", "Aus", "", "");
        seats.add(seats1);
        seats.add(seats2);
        Set<String> admins = new HashSet<>();
        admins.add("test1@example.com");
        Set<String> categories = new HashSet<>();
        categories.add("testcategory");
        Set<String> tags = new HashSet<>();
        tags.add("testtags");
        var event_id = controller.createEvent(session, new CreateEventRequest(authTokenString, "test event", "test picture", location
                                            , "2011-12-03T10:15:30", 
                                            "2011-12-04T10:15:30", "description", seats, admins, categories, tags)).event_id;
        var event_uuid = UUID.fromString(event_id);
        session = TestHelper.commitMakeSession(model, session);  
        var eventOpt = session.getById(Event.class, event_uuid);
        assertTrue(eventOpt.isPresent());
        var event = eventOpt.orElse(null);
        assertEquals("test event", event.getEventName());
        // assertEquals(location, event.getLocation());
        LocalDateTime startDate = LocalDateTime.parse("2011-12-03T10:15:30");
        LocalDateTime endDate = LocalDateTime.parse("2011-12-04T10:15:30");
        assertEquals(startDate, event.getEventStart());
        assertEquals(endDate, event.getEventEnd());
        assertEquals("description", event.getEventDescription());
        assertEquals(150, event.getSeatAvailability());
        // List<Tag> tagList = new ArrayList<Tag>(event.getTags());
        // var tag = session.getById(Tag.class, tagList.get(0));
        // assertEquals("testtags", tag.getTags());
        // assertEquals(categories, event.getCategories());
    }

    @Test
    public void testTwoCreateEvent () {
        var session = model.makeSession();
        var authTokenString = controller.userRegister(session,
        new UserRegisterRequest("test", "first", "last", "test1@example.com",
                "Password123!", "2022-04-14")).authToken;
        CreateEventRequest.SeatingDetails seats1 = new CreateEventRequest.SeatingDetails("sectionA", 100);
        CreateEventRequest.SeatingDetails seats2 = new CreateEventRequest.SeatingDetails("sectionB", 50);
        List<CreateEventRequest.SeatingDetails> seats = new ArrayList<CreateEventRequest.SeatingDetails>();
        SerializedLocation location = new SerializedLocation("test street", 12, null, "2000", "NSW", "Aus", "", "");
        seats.add(seats1);
        seats.add(seats2);
        Set<String> admins = new HashSet<>();
        admins.add("test1@example.com");
        Set<String> categories = new HashSet<>();
        categories.add("testcategory");
        Set<String> tags = new HashSet<>();
        tags.add("testtags");
        var event_id = controller.createEvent(session, new CreateEventRequest(authTokenString, "test event", "test picture", location
                                            , "2011-12-03T10:15:30", 
                                            "2011-12-04T10:15:30", "description", seats, admins, categories, tags)).event_id;
        var event_uuid = UUID.fromString(event_id);
        session = TestHelper.commitMakeSession(model, session);  
        var eventOpt = session.getById(Event.class, event_uuid);
        assertTrue(eventOpt.isPresent());
        var event = eventOpt.orElse(null);
        assertEquals("test event", event.getEventName());
        // assertEquals(location, event.getLocation());
        LocalDateTime startDate = LocalDateTime.parse("2011-12-03T10:15:30");
        LocalDateTime endDate = LocalDateTime.parse("2011-12-04T10:15:30");
        assertEquals(startDate, event.getEventStart());
        assertEquals(endDate, event.getEventEnd());
        assertEquals("description", event.getEventDescription());
        assertEquals(150, event.getSeatAvailability());

        var event_id2 = controller.createEvent(session, new CreateEventRequest(authTokenString, "test event2", "test picture", location
                                            , "2011-12-03T10:15:30", 
                                            "2011-12-04T10:15:30", "description", seats, admins, categories, tags)).event_id;
        var event_uuid2 = UUID.fromString(event_id2);
        session = TestHelper.commitMakeSession(model, session);  
        var eventOpt2 = session.getById(Event.class, event_uuid2);
        assertTrue(eventOpt2.isPresent());
        var event2 = eventOpt2.orElse(null);
        assertEquals("test event2", event2.getEventName());
        // assertEquals(location, event.getLocation());
        assertEquals(startDate, event.getEventStart());
        assertEquals(endDate, event.getEventEnd());
        assertEquals("description", event2.getEventDescription());
        assertEquals(150, event2.getSeatAvailability());                                    
    }
}
