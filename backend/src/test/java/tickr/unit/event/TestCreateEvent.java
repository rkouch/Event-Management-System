package tickr.unit.event;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import tickr.TestHelper;
import tickr.application.TickrController;
import tickr.application.entities.Category;
import tickr.application.entities.Event;
import tickr.application.entities.SeatingPlan;
import tickr.application.entities.Tag;
import tickr.application.entities.User;
import tickr.application.serialised.SerializedLocation;
import tickr.application.serialised.requests.CreateEventRequest;
import tickr.application.serialised.requests.UserRegisterRequest;
import tickr.persistence.DataModel;
import tickr.persistence.HibernateModel;
import tickr.server.exceptions.BadRequestException;
import tickr.server.exceptions.ForbiddenException;
import tickr.server.exceptions.UnauthorizedException;
import tickr.util.CryptoHelper;
import tickr.util.FileHelper;

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

    @AfterAll
    public static void clearStaticFiles () {
        TestHelper.clearStaticFiles();
    }

    @Test
    public void testMissingParamaters () {
        var session = model.makeSession();
        var authTokenString = controller.userRegister(session,
        new UserRegisterRequest("test", "first", "last", "test1@example.com",
                "Password123!", "2022-04-14")).authToken;
        session = TestHelper.commitMakeSession(model, session);
        CreateEventRequest.SeatingDetails seats1 = new CreateEventRequest.SeatingDetails("sectionA", 100, 50);
        CreateEventRequest.SeatingDetails seats2 = new CreateEventRequest.SeatingDetails("sectionB", 50, 50);
        List<CreateEventRequest.SeatingDetails> seats = new ArrayList<CreateEventRequest.SeatingDetails>();
        SerializedLocation location = new SerializedLocation("test street", 12, null, "Sydney", "2000", "NSW", "Aus", "", "");
        seats.add(seats1);
        seats.add(seats2);
        Set<String> admins = new HashSet<>();
        admins.add("test1@example.com");
        Set<String> categories = new HashSet<>();
        categories.add("testcategory");
        Set<String> tags = new HashSet<>();
        tags.add("testtags");
        tickr.persistence.ModelSession finalSession = session;
        assertThrows(ForbiddenException.class, () -> controller.createEvent(finalSession, new CreateEventRequest(authTokenString, "test event",
            null, location, "2011-12-03T10:15:30", "2011-12-04T10:15:30", "description", seats, Set.of(UUID.randomUUID().toString()), categories, tags)));
        assertThrows(ForbiddenException.class, () -> controller.createEvent(finalSession, new CreateEventRequest(authTokenString, "test event",
            null, location, "2011-12-03T10:15:30", "2011-12-04T10:15:30", "description", seats, admins, categories, tags)));
        assertThrows(UnauthorizedException.class, () -> controller.createEvent(finalSession, new CreateEventRequest("authTokenString", "test event",
            null, location, "2011-12-03T10:15:30", "2011-12-04T10:15:30", "description", seats, admins, categories, tags)));
        assertThrows(BadRequestException.class, () -> controller.createEvent(finalSession, new CreateEventRequest("", "test event",
            null, location, "2011-12-03T10:15:30", "2011-12-04T10:15:30", "description", seats, admins, categories, tags)));
        assertThrows(BadRequestException.class, () -> controller.createEvent(finalSession, new CreateEventRequest(authTokenString, "",
            null, location, "2011-12-03T10:15:30", "2011-12-04T10:15:30", "description", seats, admins, categories, tags)));
        assertThrows(BadRequestException.class, () -> controller.createEvent(finalSession, new CreateEventRequest(authTokenString, null,
            null, location, "2011-12-03T10:15:30", "2011-12-04T10:15:30", "description", seats, admins, categories, tags)));
        assertThrows(BadRequestException.class, () -> controller.createEvent(finalSession, new CreateEventRequest(authTokenString, "",
            null, null, "2011-12-03T10:15:30", "2011-12-04T10:15:30", "description", seats, admins, categories, tags)));
        assertThrows(BadRequestException.class, () -> controller.createEvent(finalSession, new CreateEventRequest(authTokenString, "",
            null, location, null, "2011-12-04T10:15:30", "description", seats, admins, categories, tags)));
        assertThrows(BadRequestException.class, () -> controller.createEvent(finalSession, new CreateEventRequest(authTokenString, "",
            null, location, "2011-12-03T10:15:30", null, "description", seats, admins, categories, tags)));
        assertThrows(BadRequestException.class, () -> controller.createEvent(finalSession, new CreateEventRequest(authTokenString, "",
            null, location, "2011-12-03T10:15:30", "2011-12-04T10:15:30", "description", seats, null, categories, tags)));
        assertThrows(BadRequestException.class, () -> controller.createEvent(finalSession, new CreateEventRequest(authTokenString, "",
            null, location, "2011-12-03T10:15:30", "2011-12-04T10:15:30", "description", seats, admins, null, tags)));
        assertThrows(BadRequestException.class, () -> controller.createEvent(finalSession, new CreateEventRequest(authTokenString, "",
            null, location, "2011-12-03T10:15:30", "2011-12-04T10:15:30", "description", seats, admins, categories, null)));
        CreateEventRequest.SeatingDetails invalidSeats2 = new CreateEventRequest.SeatingDetails(null, 50, 50);
        List<CreateEventRequest.SeatingDetails> invalidSeatsList1 = new ArrayList<CreateEventRequest.SeatingDetails>();
        invalidSeatsList1.add(invalidSeats2);
        assertThrows(BadRequestException.class, () -> controller.createEvent(finalSession, new CreateEventRequest(authTokenString, "asd",
            null, location, "2011-12-03T10:15:30", "2011-12-04T10:15:30", "description", invalidSeatsList1, admins, categories, tags)));

        CreateEventRequest.SeatingDetails invalidSeats1 = new CreateEventRequest.SeatingDetails("", 100, 50);
        List<CreateEventRequest.SeatingDetails> invalidSeatsList2 = new ArrayList<CreateEventRequest.SeatingDetails>();
        invalidSeatsList2.add(invalidSeats1);
        assertThrows(BadRequestException.class, () -> controller.createEvent(finalSession, new CreateEventRequest(authTokenString, "asd",
            null, location, "2011-12-03T10:15:30", "2011-12-04T10:15:30", "description", invalidSeatsList2, admins, categories, tags)));
    }

    @Test 
    public void testMissingLocationParamaters () {
        var session = model.makeSession();
        var authTokenString = controller.userRegister(session,
        new UserRegisterRequest("test", "first", "last", "test1@example.com",
                "Password123!", "2022-04-14")).authToken;
        session = TestHelper.commitMakeSession(model, session);
        CreateEventRequest.SeatingDetails seats1 = new CreateEventRequest.SeatingDetails("sectionA", 100, 50);
        CreateEventRequest.SeatingDetails seats2 = new CreateEventRequest.SeatingDetails("sectionB", 50, 50);
        List<CreateEventRequest.SeatingDetails> seats = new ArrayList<CreateEventRequest.SeatingDetails>();
        seats.add(seats1);
        seats.add(seats2);
        Set<String> admins = new HashSet<>();
        admins.add("test1@example.com");
        Set<String> categories = new HashSet<>();
        categories.add("testcategory");
        Set<String> tags = new HashSet<>();
        tags.add("testtags");
        SerializedLocation location1 = new SerializedLocation("test street", 12, null, "Sydney",null, "NSW", "Aus", "", "");
        SerializedLocation location2 = new SerializedLocation("test street", 12, null, "Sydney","2000", null, "Aus", "", "");
        SerializedLocation location3 = new SerializedLocation("test street", 12, null, "Sydney","2000", "NSW", null, "", "");
        SerializedLocation location4 = new SerializedLocation("test street", 12, null, "Sydney","2000", "NSW", "Aus", null, "");
        SerializedLocation location5 = new SerializedLocation("test street", 12, null, "Sydney", "2000", "NSW", "Aus", "", null);
        var finalSession = session;
        assertThrows(BadRequestException.class, () -> controller.createEvent(finalSession, new CreateEventRequest(authTokenString, "asd",
            null, location1, "2011-12-03T10:15:30", "2011-12-04T10:15:30", "description", seats, admins, categories, tags)));
        assertThrows(BadRequestException.class, () -> controller.createEvent(finalSession, new CreateEventRequest(authTokenString, "asd",
            null, location2, "2011-12-03T10:15:30", "2011-12-04T10:15:30", "description", seats, admins, categories, tags)));
        assertThrows(BadRequestException.class, () -> controller.createEvent(finalSession, new CreateEventRequest(authTokenString, "asd",
            null, location3, "2011-12-03T10:15:30", "2011-12-04T10:15:30", "description", seats, admins, categories, tags)));
        assertThrows(BadRequestException.class, () -> controller.createEvent(finalSession, new CreateEventRequest(authTokenString, "asd",
            null, location4, "2011-12-03T10:15:30", "2011-12-04T10:15:30", "description", seats, admins, categories, tags)));
        assertThrows(BadRequestException.class, () -> controller.createEvent(finalSession, new CreateEventRequest(authTokenString, "asd",
            null, location5, "2011-12-03T10:15:30", "2011-12-04T10:15:30", "description", seats, admins, categories, tags)));
    }

    @Test 
    public void testInvalidToken () {
        var session = model.makeSession();
        CreateEventRequest.SeatingDetails seats1 = new CreateEventRequest.SeatingDetails("sectionA", 100, 50);
        CreateEventRequest.SeatingDetails seats2 = new CreateEventRequest.SeatingDetails("sectionB", 50, 50);
        List<CreateEventRequest.SeatingDetails> seats = new ArrayList<CreateEventRequest.SeatingDetails>();
        SerializedLocation location = new SerializedLocation("test street", 12, null, "Sydney", "2000", "NSW", "Aus", "", "");
        seats.add(seats1);
        seats.add(seats2);
        Set<String> admins = new HashSet<>();
        admins.add("test1@example.com");
        Set<String> categories = new HashSet<>();
        categories.add("testcategory");
        Set<String> tags = new HashSet<>();
        tags.add("testtags");
        assertThrows(UnauthorizedException.class, () -> controller.createEvent(session, new CreateEventRequest("authTokenString", "test event", 
        null, location
        , "2011-12-03T10:15:30", 
        "2011-12-04T10:15:30", "description", seats, admins, categories, tags)));
        assertThrows(UnauthorizedException.class, () -> controller.createEvent(session, new CreateEventRequest(null, "test event", 
        null, location
        , "2011-12-03T10:15:30", 
        "2011-12-04T10:15:30", "description", seats, admins, categories, tags)));
    }

    @Test 
    public void testInvalidDates () {
        var session = model.makeSession();
        var authTokenString = controller.userRegister(session,
        new UserRegisterRequest("test", "first", "last", "test1@example.com",
                "Password123!", "2022-04-14")).authToken;
        CreateEventRequest.SeatingDetails seats1 = new CreateEventRequest.SeatingDetails("sectionA", 100, 50);
        CreateEventRequest.SeatingDetails seats2 = new CreateEventRequest.SeatingDetails("sectionB", 50, 50);
        List<CreateEventRequest.SeatingDetails> seats = new ArrayList<CreateEventRequest.SeatingDetails>();
        SerializedLocation location = new SerializedLocation("test street", 12, null, "Sydney", "2000", "NSW", "Aus", "", "");
        seats.add(seats1);
        seats.add(seats2);
        Set<String> admins = new HashSet<>();
        admins.add("test1@example.com");
        Set<String> categories = new HashSet<>();
        categories.add("testcategory");
        Set<String> tags = new HashSet<>();
        tags.add("testtags");
        assertThrows(BadRequestException.class, () -> controller.createEvent(session, new CreateEventRequest(authTokenString, "test event", 
        null, location
        , "2011-12-04T10:15:30", 
        "2011-12-03T10:15:30", "description", seats, admins, categories, tags)));

        assertThrows(ForbiddenException.class, () -> controller.createEvent(session, new CreateEventRequest(authTokenString, "test event", 
        null, location
        , "2011-12-04 10:15:30", 
        "2011-12-03 10:15:30", "description", seats, admins, categories, tags)));
    }

    @Test
    public void testCreateEvent () {
        var session = model.makeSession();
        var authTokenString = controller.userRegister(session,
        new UserRegisterRequest("test", "first", "last", "test1@example.com",
                "Password123!", "2022-04-14")).authToken;
        var authToken = CryptoHelper.makeJWTParserBuilder()
        .build()
        .parseClaimsJws(authTokenString);
        var id = authToken.getBody().getSubject();
        assertNotNull(id);
        CreateEventRequest.SeatingDetails seats1 = new CreateEventRequest.SeatingDetails("sectionA", 100, 50);
        CreateEventRequest.SeatingDetails seats2 = new CreateEventRequest.SeatingDetails("sectionB", 50, 70);
        List<CreateEventRequest.SeatingDetails> seats = new ArrayList<CreateEventRequest.SeatingDetails>();
        SerializedLocation location = new SerializedLocation("test street", 12, null, "Sydney", "2000", "NSW", "Aus", "", "");
        seats.add(seats1);
        seats.add(seats2);
        Set<String> admins = new HashSet<>();
        admins.add(id);
        Set<String> categories = new HashSet<>();
        categories.add("testcategory");
        Set<String> tags = new HashSet<>();
        tags.add("testtags");
        var event_id = controller.createEvent(session, new CreateEventRequest(authTokenString, "test event", null, location
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
        for (Tag tag : event.getTags()) {
            assertEquals("testtags", tag.getTags());
        }
        for (Category cat : event.getCategories()) {
            assertEquals("testcategory", cat.getCategory());
        }
        for (User admin : event.getAdmins()) {
            assertEquals(id, admin.getId().toString());
        }
        Event event1 = session.getById(Event.class, UUID.fromString(event_id)).orElse(null);
        List<SeatingPlan> seatings = session.getAllWith(SeatingPlan.class, "event", event1);
        assertEquals(seatings.get(0).section, "sectionA");
        assertEquals(seatings.get(0).availableSeats, 100);
        assertEquals(seatings.get(0).ticketPrice, 50);
        assertEquals(seatings.get(1).section, "sectionB");
        assertEquals(seatings.get(1).availableSeats, 50);
        assertEquals(seatings.get(1).ticketPrice, 70);
        assertEquals(event.getLocation().getStreetName(), location.streetName);
        assertEquals(event.getLocation().getStreetNo(), location.streetNo);
        assertEquals(event.getLocation().getUnitNo(), location.unitNo);
        assertEquals(event.getLocation().getPostcode(), location.postcode);
        assertEquals(event.getLocation().getState(), location.state);
        assertEquals(event.getLocation().getCountry(), location.country);
        assertEquals(event.getLocation().getLatitude(), location.latitude);
        assertEquals(event.getLocation().getLongitude(), location.longitude);
    }

    @Test
    public void testTwoCreateEvent () {
        var session = model.makeSession();
        var authTokenString = controller.userRegister(session,
        new UserRegisterRequest("test", "first", "last", "test1@example.com",
                "Password123!", "2022-04-14")).authToken;
        var authToken = CryptoHelper.makeJWTParserBuilder()
        .build()
        .parseClaimsJws(authTokenString);
        var id = authToken.getBody().getSubject();
        assertNotNull(id);
        CreateEventRequest.SeatingDetails seats1 = new CreateEventRequest.SeatingDetails("sectionA", 100, 50);
        CreateEventRequest.SeatingDetails seats2 = new CreateEventRequest.SeatingDetails("sectionB", 50, 50);
        List<CreateEventRequest.SeatingDetails> seats = new ArrayList<CreateEventRequest.SeatingDetails>();
        SerializedLocation location = new SerializedLocation("test street", 12, null, "Sydney", "2000", "NSW", "Aus", "", "");
        seats.add(seats1);
        seats.add(seats2);
        Set<String> admins = new HashSet<>();
        admins.add(id);
        Set<String> categories = new HashSet<>();
        categories.add("testcategory");
        Set<String> tags = new HashSet<>();
        tags.add("testtags");
        var event_id = controller.createEvent(session, new CreateEventRequest(authTokenString, "test event", null, location
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
        for (Tag tag : event.getTags()) {
            assertEquals("testtags", tag.getTags());
        }
        for (Category cat : event.getCategories()) {
            assertEquals("testcategory", cat.getCategory());
        }
        for (User admin : event.getAdmins()) {
            assertEquals(id, admin.getId().toString());
        }
        Event event1 = session.getById(Event.class, UUID.fromString(event_id)).orElse(null);
        List<SeatingPlan> seatings = session.getAllWith(SeatingPlan.class, "event", event1);
        assertEquals(seatings.get(0).section, "sectionA");
        assertEquals(seatings.get(0).availableSeats, 100);
        assertEquals(seatings.get(0).ticketPrice, 50);
        assertEquals(seatings.get(1).section, "sectionB");
        assertEquals(seatings.get(1).availableSeats, 50);
        assertEquals(seatings.get(1).ticketPrice, 50);
        assertEquals(event.getLocation().getStreetName(), location.streetName);
        assertEquals(event.getLocation().getStreetNo(), location.streetNo);
        assertEquals(event.getLocation().getUnitNo(), location.unitNo);
        assertEquals(event.getLocation().getPostcode(), location.postcode);
        assertEquals(event.getLocation().getState(), location.state);
        assertEquals(event.getLocation().getCountry(), location.country);
        assertEquals(event.getLocation().getLatitude(), location.latitude);
        assertEquals(event.getLocation().getLongitude(), location.longitude);
        var event_id2 = controller.createEvent(session, new CreateEventRequest(authTokenString, "test event2", null, location
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
        
        Event event3 = session.getById(Event.class, UUID.fromString(event_id)).orElse(null);
        List<SeatingPlan> seatings2 = session.getAllWith(SeatingPlan.class, "event", event3);
        assertEquals(seatings2.get(0).section, "sectionA");
        assertEquals(seatings2.get(0).availableSeats, 100);
        assertEquals(seatings2.get(0).ticketPrice, 50);
        assertEquals(seatings2.get(1).section, "sectionB");
        assertEquals(seatings2.get(1).availableSeats, 50);
        assertEquals(seatings2.get(1).ticketPrice, 50);
        assertEquals(event2.getLocation().getStreetName(), location.streetName);
        assertEquals(event2.getLocation().getStreetNo(), location.streetNo);
        assertEquals(event2.getLocation().getUnitNo(), location.unitNo);
        assertEquals(event2.getLocation().getPostcode(), location.postcode);
        assertEquals(event2.getLocation().getState(), location.state);
        assertEquals(event2.getLocation().getCountry(), location.country);
        assertEquals(event2.getLocation().getLatitude(), location.latitude);
        assertEquals(event2.getLocation().getLongitude(), location.longitude);
    }

    @Test
    public void testCreateEventImage () {
        var session = model.makeSession();
        var authToken = controller.userRegister(session,
                new UserRegisterRequest("test", "first", "last", "test1@example.com",
                        "Password123!", "2022-04-14")).authToken;
        session = TestHelper.commitMakeSession(model, session);

        var location = new SerializedLocation("test street", 12, null, "Sydney", "2000", "NSW", "Aus", "", "");
        var event_id = controller.createEvent(session, new CreateEventRequest(authToken, "test event", FileHelper.readToDataUrl("/test_images/smile.png"),
                location, "2011-12-03T10:15:30", "2011-12-04T10:15:30", "description", List.of(), Set.of(), Set.of(), Set.of())).event_id;

        session = TestHelper.commitMakeSession(model, session);
        var event = session.getById(Event.class, UUID.fromString(event_id)).orElseThrow(AssertionError::new);
        var newFilePath = FileHelper.getStaticPath() + "/" + event.getEventPicture();

        assertTrue(TestHelper.fileDiff("/test_images/smile.png", newFilePath));
    }
}
