package tickr.integration.event;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import spark.Spark;
import tickr.application.serialised.SerializedLocation;
import tickr.application.serialised.requests.CreateEventRequest;
import tickr.application.serialised.requests.UserRegisterRequest;
import tickr.application.serialised.responses.AuthTokenResponse;
import tickr.application.serialised.responses.CreateEventResponse;
import tickr.application.serialised.responses.EventViewResponse;
import tickr.integration.HTTPHelper;
import tickr.persistence.DataModel;
import tickr.persistence.HibernateModel;
import tickr.server.Server;
import tickr.util.CryptoHelper;

public class TestViewEvent {
    private DataModel hibernateModel;
    private HTTPHelper httpHelper;

    private String event_id;

    @BeforeEach
    public void setup () {
        hibernateModel = new HibernateModel("hibernate-test.cfg.xml");

        Server.start(8080, null, hibernateModel);
        httpHelper = new HTTPHelper("http://localhost:8080");
        Spark.awaitInitialization();

       
    }

    @AfterEach
    public void cleanup () {
        Spark.stop();
        hibernateModel.cleanup();
        Spark.awaitStop();
    }

    @Test 
    public void testCreateEvent() {
        var registerResponse = httpHelper.post("/api/user/register", new UserRegisterRequest("test_username", "TestFirst",
        "TestLast", "test@example.com", "Testing123!", "2022-04-14"));
        assertEquals(200, registerResponse.getStatus());
        var authTokenString = registerResponse.getBody(AuthTokenResponse.class).authToken;
        var authToken = CryptoHelper.makeJWTParserBuilder()
        .build()
        .parseClaimsJws(authTokenString);
        var id = authToken.getBody().getSubject();
        assertNotNull(id);
        CreateEventRequest.SeatingDetails seats1 = new CreateEventRequest.SeatingDetails("sectionA", 100);
        CreateEventRequest.SeatingDetails seats2 = new CreateEventRequest.SeatingDetails("sectionB", 50);
        List<CreateEventRequest.SeatingDetails> seats = new ArrayList<CreateEventRequest.SeatingDetails>();
        SerializedLocation location = new SerializedLocation("test street", 12, null, "2000", "NSW", "Aus", "", "");
        seats.add(seats1);
        seats.add(seats2);
        Set<String> admins = new HashSet<>();
        admins.add(id);
        Set<String> categories = new HashSet<>();
        categories.add("testcategory");
        Set<String> tags = new HashSet<>();
        tags.add("testtags");

        var eventResponse = httpHelper.post("/api/event/create", new CreateEventRequest(authTokenString, "test event", "test picture", location
        , "2011-12-03T10:15:30", 
        "2011-12-04T10:15:30", "description", seats, admins, categories, tags));
        assertEquals(200, eventResponse.getStatus());
        var response = httpHelper.get("/api/event/view", Map.of("event_id", eventResponse.getBody(CreateEventResponse.class).event_id)).getBody(EventViewResponse.class);
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
