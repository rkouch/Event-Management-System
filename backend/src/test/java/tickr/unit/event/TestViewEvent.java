package tickr.unit.event;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import tickr.TestHelper;
import tickr.application.TickrController;
import tickr.application.serialised.SerializedLocation;
import tickr.application.serialised.requests.CreateEventRequest;
import tickr.application.serialised.requests.UserRegisterRequest;
import tickr.persistence.DataModel;
import tickr.persistence.HibernateModel;
import tickr.server.exceptions.BadRequestException;
import tickr.server.exceptions.ForbiddenException;
import tickr.util.CryptoHelper;

public class TestViewEvent {
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
    public void invalidEventId () {
        var session = model.makeSession();
        var authTokenString = controller.userRegister(session,
        new UserRegisterRequest("test", "first", "last", "test1@example.com",
                "Password123!", "2022-04-14")).authToken;
        var authToken = CryptoHelper.makeJWTParserBuilder()
        .build()
        .parseClaimsJws(authTokenString);
        var id = authToken.getBody().getSubject();
        assertNotNull(id);
        CreateEventRequest.SeatingDetails seats1 = new CreateEventRequest.SeatingDetails("sectionA", 100);
        CreateEventRequest.SeatingDetails seats2 = new CreateEventRequest.SeatingDetails("sectionB", 50);

        List<CreateEventRequest.SeatingDetails> seats = new ArrayList<CreateEventRequest.SeatingDetails>();
        seats.add(seats1);
        seats.add(seats2);

        SerializedLocation location = new SerializedLocation("test street", 12, null, "2000", "NSW", "Aus", "", "");
        
        Set<String> admins = new HashSet<>();
        admins.add(id);

        Set<String> categories = new HashSet<>();
        categories.add("testcategory");

        Set<String> tags = new HashSet<>();
        tags.add("testtags");

        var event_id = controller.createEvent(session, new CreateEventRequest(authTokenString, "test event", "test picture", location
                                            , "2011-12-03T10:15:30", 
                                            "2011-12-04T10:15:30", "description", seats, admins, categories, tags)).event_id;
        session = TestHelper.commitMakeSession(model, session); 
        var finalSession = session;
        assertThrows(BadRequestException.class, () -> controller.eventView(finalSession, Map.of("event_i", event_id)));
    }

    @Test 
    public void testViewEvent () {
        var session = model.makeSession();
        var authTokenString = controller.userRegister(session,
        new UserRegisterRequest("test", "first", "last", "test1@example.com",
                "Password123!", "2022-04-14")).authToken;
        var authToken = CryptoHelper.makeJWTParserBuilder()
        .build()
        .parseClaimsJws(authTokenString);
        var id = authToken.getBody().getSubject();
        assertNotNull(id);
        CreateEventRequest.SeatingDetails seats1 = new CreateEventRequest.SeatingDetails("sectionA", 100);
        CreateEventRequest.SeatingDetails seats2 = new CreateEventRequest.SeatingDetails("sectionB", 50);

        List<CreateEventRequest.SeatingDetails> seats = new ArrayList<CreateEventRequest.SeatingDetails>();
        seats.add(seats1);
        seats.add(seats2);

        SerializedLocation location = new SerializedLocation("test street", 12, null, "2000", "NSW", "Aus", "", "");
        
        Set<String> admins = new HashSet<>();
        admins.add(id);

        Set<String> categories = new HashSet<>();
        categories.add("testcategory");

        Set<String> tags = new HashSet<>();
        tags.add("testtags");

        var event_id = controller.createEvent(session, new CreateEventRequest(authTokenString, "test event", "test picture", location
                                            , "2011-12-03T10:15:30", 
                                            "2011-12-04T10:15:30", "description", seats, admins, categories, tags)).event_id;
        session = TestHelper.commitMakeSession(model, session); 

        var response = controller.eventView(session, Map.of("event_id", event_id)); 

        assertEquals("test event", response.eventName);
        assertEquals("test picture", response.picture);
        assertEquals(location.streetName, response.location.streetName);
        assertEquals(location.streetNo, response.location.streetNo);
        assertEquals(location.unitNo, response.location.unitNo);
        assertEquals(location.postcode, response.location.postcode);
        assertEquals(location.state, response.location.state);
        assertEquals(location.country, response.location.country);
        assertEquals(location.longitude, response.location.longitude);
        assertEquals(location.latitude, response.location.latitude);
        assertEquals("2011-12-03T10:15:30", response.startDate);
        assertEquals("2011-12-04T10:15:30", response.endDate);

        assertEquals(seats.get(0).section, response.seatingDetails.get(0).section);
        assertEquals(seats.get(0).availability, response.seatingDetails.get(0).availability);
        assertEquals(seats.get(1).section, response.seatingDetails.get(1).section);
        assertEquals(seats.get(1).availability, response.seatingDetails.get(1).availability);

        Set<String> testAdmins = new HashSet<>();
        testAdmins.add(id); 
        assertEquals(testAdmins, response.admins);

        Set<String> testCategories = new HashSet<>(); 
        testCategories.add("testcategory");
        assertEquals(testCategories, response.categories);

        Set<String> testTags = new HashSet<>(); 
        testTags.add("testtags");
        assertEquals(testTags, response.tags);

    }
}
