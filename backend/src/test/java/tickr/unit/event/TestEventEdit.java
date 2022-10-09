package tickr.unit.event;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import tickr.TestHelper;
import tickr.application.TickrController;
import tickr.application.serialised.SerializedLocation;
import tickr.application.serialised.requests.CreateEventRequest;
import tickr.application.serialised.requests.EditEventRequest;
import tickr.application.serialised.requests.UserRegisterRequest;
import tickr.persistence.DataModel;
import tickr.persistence.HibernateModel;
import tickr.util.CryptoHelper;

public class TestEventEdit {
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
    public void testEdit () {
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

        EditEventRequest.SeatingDetails seats3 = new EditEventRequest.SeatingDetails("sectionC", 101);
        EditEventRequest.SeatingDetails seats4 = new EditEventRequest.SeatingDetails("sectionD", 51);
        List<EditEventRequest.SeatingDetails> seatsList = new ArrayList<EditEventRequest.SeatingDetails>();
        seatsList.add(seats3);
        seatsList.add(seats4);

        SerializedLocation location = new SerializedLocation("test street", 12, null, "Sydney", "2000", "NSW", "Aus", "", "");
        SerializedLocation location2 = new SerializedLocation("update street", 12, null, "Melbourne", "20200", "NS2W", "A2us", "", "");
        
        Set<String> admins = new HashSet<>();
        admins.add(id);
        Set<String> categories = new HashSet<>();
        categories.add("testcategory");
        Set<String> updateCategories = new HashSet<>();
        updateCategories.add("updatecategory");
        Set<String> tags = new HashSet<>();
        tags.add("testtags");
        Set<String> updateTags = new HashSet<>();
        updateTags.add("updatetags");
        var event_id = controller.createEvent(session, new CreateEventRequest(authTokenString, "test event", null, location
                                            , "2011-12-03T10:15:30", 
                                            "2011-12-04T10:15:30", "description", seats, admins, categories, tags)).event_id;
        session = TestHelper.commitMakeSession(model, session);  
        controller.editEvent(session, new EditEventRequest(event_id, authTokenString, "update name", null, location2, "2011-12-04T10:15:30","2011-12-05T10:15:30",
        "updated description", seatsList, admins, updateCategories, updateTags));


        var response = controller.eventView(session, Map.of("event_id", event_id)); 
        assertEquals(id, response.host_id);
        assertEquals("update name", response.eventName);
        assertEquals("", response.picture);
        assertEquals(location2.streetName, response.location.streetName);
        assertEquals(location2.streetNo, response.location.streetNo);
        assertEquals(location2.unitNo, response.location.unitNo);
        assertEquals(location2.postcode, response.location.postcode);
        assertEquals(location2.state, response.location.state);
        assertEquals(location2.country, response.location.country);
        assertEquals(location2.longitude, response.location.longitude);
        assertEquals(location2.latitude, response.location.latitude);
        assertEquals("2011-12-04T10:15:30", response.startDate);
        assertEquals("2011-12-05T10:15:30", response.endDate);

        assertEquals(seatsList.get(0).section, response.seatingDetails.get(0).section);
        assertEquals(seatsList.get(0).availability, response.seatingDetails.get(0).availability);
        assertEquals(seatsList.get(1).section, response.seatingDetails.get(1).section);
        assertEquals(seatsList.get(1).availability, response.seatingDetails.get(1).availability);

        Set<String> testAdmins = new HashSet<>();
        testAdmins.add(id); 
        assertEquals(testAdmins, response.admins);

        Set<String> testCategories = new HashSet<>(); 
        testCategories.add("updatecategory");
        assertEquals(testCategories, response.categories);

        Set<String> testTags = new HashSet<>(); 
        testTags.add("updatetags");
        assertEquals(testTags, response.tags);
    }
}
