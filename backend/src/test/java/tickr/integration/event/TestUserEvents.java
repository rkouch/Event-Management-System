package tickr.integration.event;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import spark.Spark;
import tickr.CreateEventReqBuilder;
import tickr.application.TickrController;
import tickr.application.serialised.requests.CreateEventRequest;
import tickr.application.serialised.requests.EditHostRequest;
import tickr.application.serialised.requests.EventDeleteRequest;
import tickr.application.serialised.requests.UserRegisterRequest;
import tickr.application.serialised.responses.AuthTokenResponse;
import tickr.application.serialised.responses.CreateEventResponse;
import tickr.application.serialised.responses.EventHostingsResponse;
import tickr.application.serialised.responses.EventViewResponse;
import tickr.application.serialised.responses.UserEventsResponse;
import tickr.application.serialised.responses.UserIdResponse;
import tickr.persistence.DataModel;
import tickr.persistence.HibernateModel;
import tickr.server.Server;
import tickr.util.HTTPHelper;

public class TestUserEvents {
    private DataModel hibernateModel;
    private HTTPHelper httpHelper;

    private String authToken; 

    @BeforeEach
    public void setup () {
        hibernateModel = new HibernateModel("hibernate-test.cfg.xml");

        Server.start(8080, null, hibernateModel);
        httpHelper = new HTTPHelper("http://localhost:8080");
        Spark.awaitInitialization();

        var response = httpHelper.post("/api/user/register", new UserRegisterRequest("test", "first", "last", "test1@example.com",
                "Password123!", "2022-04-14"));
        assertEquals(200, response.getStatus());
        authToken = response.getBody(AuthTokenResponse.class).authToken;

        List<CreateEventRequest.SeatingDetails> seatingDetails = new ArrayList<>();
        seatingDetails.add(new CreateEventRequest.SeatingDetails("SectionA", 10, 50, true));
        seatingDetails.add(new CreateEventRequest.SeatingDetails("SectionB", 20, 30, true));

        response = httpHelper.post("/api/event/create", new CreateEventReqBuilder()
                .withEventName("Test Event")
                .withSeatingDetails(seatingDetails)
                .withStartDate(LocalDateTime.now().plusDays(1))
                .withEndDate(LocalDateTime.now().plusDays(2))
                .build(authToken));
        assertEquals(200, response.getStatus());

        response = httpHelper.post("/api/event/create", new CreateEventReqBuilder()
                .withEventName("Test Event")
                .withSeatingDetails(seatingDetails)
                .withStartDate(LocalDateTime.now().plusDays(3))
                .withEndDate(LocalDateTime.now().plusDays(4))
                .build(authToken));
        assertEquals(200, response.getStatus());

        response = httpHelper.post("/api/event/create", new CreateEventReqBuilder()
                .withEventName("Test Event")
                .withSeatingDetails(seatingDetails)
                .withStartDate(LocalDateTime.now().plusDays(5))
                .withEndDate(LocalDateTime.now().plusDays(6))
                .build(authToken));
        assertEquals(200, response.getStatus());

        response = httpHelper.post("/api/event/create", new CreateEventReqBuilder()
                .withEventName("Test Event")
                .withSeatingDetails(seatingDetails)
                .withStartDate(LocalDateTime.now().plusDays(15))
                .withEndDate(LocalDateTime.now().plusDays(16))
                .build(authToken));
        assertEquals(200, response.getStatus());

        response = httpHelper.post("/api/event/create", new CreateEventReqBuilder()
                .withEventName("Test Event")
                .withSeatingDetails(seatingDetails)
                .withStartDate(LocalDateTime.now().plusDays(20))
                .withEndDate(LocalDateTime.now().plusDays(21))
                .build(authToken));
        assertEquals(200, response.getStatus());
    }

    @AfterEach
    public void cleanup () {
        Spark.stop();
        hibernateModel.cleanup();
        Spark.awaitStop();
    }

    @Test 
    public void testUserEvents () {
        var response = httpHelper.get("/api/home", Map.of(
            "page_start", Integer.toString(0),
            "max_results", Integer.toString(5),
            "before", LocalDateTime.now().plusDays(21).format(DateTimeFormatter.ISO_DATE_TIME)
        ));
        assertEquals(200, response.getStatus());
        assertEquals(response.getBody(UserEventsResponse.class).eventIds.size(), 5);

        response = httpHelper.get("/api/home", Map.of(
            "page_start", Integer.toString(0),
            "max_results", Integer.toString(5),
            "before", LocalDateTime.now().plusDays(17).format(DateTimeFormatter.ISO_DATE_TIME)
        ));
        assertEquals(200, response.getStatus());
        assertEquals(response.getBody(UserEventsResponse.class).eventIds.size(), 4);

        response = httpHelper.get("/api/home", Map.of(
            "page_start", Integer.toString(0),
            "max_results", Integer.toString(5),
            "before", LocalDateTime.now().plusDays(14).format(DateTimeFormatter.ISO_DATE_TIME)
        ));
        assertEquals(200, response.getStatus());
        assertEquals(response.getBody(UserEventsResponse.class).eventIds.size(), 3);

        response = httpHelper.get("/api/home", Map.of(
            "page_start", Integer.toString(0),
            "max_results", Integer.toString(5),
            "before", LocalDateTime.now().plusMinutes(10).format(DateTimeFormatter.ISO_DATE_TIME)
        ));
        assertEquals(200, response.getStatus());
        assertEquals(response.getBody(UserEventsResponse.class).eventIds.size(), 0);
    }

    @Test 
    public void testExceptions () {
        var response = httpHelper.get("/api/home", Map.of(
            "max_results", Integer.toString(5),
            "before", LocalDateTime.now().plusDays(21).format(DateTimeFormatter.ISO_DATE_TIME)
        ));
        assertEquals(400, response.getStatus());

        response = httpHelper.get("/api/home", Map.of(
            "page_start", Integer.toString(0),
            "before", LocalDateTime.now().plusDays(21).format(DateTimeFormatter.ISO_DATE_TIME)
        ));
        assertEquals(400, response.getStatus());

        response = httpHelper.get("/api/home", Map.of(
            "page_start", Integer.toString(0),
            "max_results", Integer.toString(5),
            "before", "20-20-2022"
        ));
        assertEquals(400, response.getStatus());

        response = httpHelper.get("/api/home", Map.of(
            "page_start", Integer.toString(0),
            "max_results", Integer.toString(5),
            "before", LocalDateTime.now().minusDays(3).format(DateTimeFormatter.ISO_DATE_TIME)
        ));
        assertEquals(403, response.getStatus());

        response = httpHelper.get("/api/home", Map.of(
            "page_start", Integer.toString(0),
            "max_results", Integer.toString(-1),
            "before", LocalDateTime.now().plusDays(21).format(DateTimeFormatter.ISO_DATE_TIME)
        ));
        assertEquals(400, response.getStatus());

        response = httpHelper.get("/api/home", Map.of(
            "page_start", Integer.toString(-1),
            "max_results", Integer.toString(5),
            "before", LocalDateTime.now().plusDays(21).format(DateTimeFormatter.ISO_DATE_TIME)
        ));
        assertEquals(400, response.getStatus());

    }
}
