package tickr.integration.event;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import spark.Spark;
import tickr.CreateEventReqBuilder;
import tickr.application.TickrController;
import tickr.application.apis.ApiLocator;
import tickr.application.apis.location.ILocationAPI;
import tickr.application.apis.purchase.IPurchaseAPI;
import tickr.application.serialised.combined.TicketPurchase;
import tickr.application.serialised.combined.TicketReserve;
import tickr.application.serialised.requests.CreateEventRequest;
import tickr.application.serialised.requests.EditEventRequest;
import tickr.application.serialised.requests.EditHostRequest;
import tickr.application.serialised.requests.EventDeleteRequest;
import tickr.application.serialised.requests.UserRegisterRequest;
import tickr.application.serialised.responses.AuthTokenResponse;
import tickr.application.serialised.responses.CreateEventResponse;
import tickr.application.serialised.responses.CustomerEventsResponse;
import tickr.application.serialised.responses.EventHostingsResponse;
import tickr.application.serialised.responses.EventViewResponse;
import tickr.application.serialised.responses.UserEventsResponse;
import tickr.application.serialised.responses.UserIdResponse;
import tickr.mock.MockHttpPurchaseAPI;
import tickr.mock.MockLocationApi;
import tickr.persistence.DataModel;
import tickr.persistence.HibernateModel;
import tickr.server.Server;
import tickr.util.HTTPHelper;
public class TestEventHostingsPast {
    private DataModel hibernateModel;
    private HTTPHelper httpHelper;
    private MockHttpPurchaseAPI purchaseAPI;

    private String authToken; 
    private String eventId1; 
    private String eventId2;
    private String eventId3;



    @BeforeEach
    public void setup () {
        hibernateModel = new HibernateModel("hibernate-test.cfg.xml");
        purchaseAPI = new MockHttpPurchaseAPI("http://localhost:8080");
        ApiLocator.addLocator(IPurchaseAPI.class, () -> purchaseAPI);
        ApiLocator.addLocator(ILocationAPI.class, () -> new MockLocationApi(hibernateModel));

        Server.start(8080, null, hibernateModel);
        httpHelper = new HTTPHelper("http://localhost:8080");
        Spark.awaitInitialization();

        var response = httpHelper.post("/api/user/register", new UserRegisterRequest("test", "first", "last", "test1@example.com",
                "Password123!", "2022-04-14"));
        assertEquals(200, response.getStatus());
        authToken = response.getBody(AuthTokenResponse.class).authToken;

        List<CreateEventRequest.SeatingDetails> seatingDetails = new ArrayList<>();
        seatingDetails.add(new CreateEventRequest.SeatingDetails("test_section", 10, 50, true));
        seatingDetails.add(new CreateEventRequest.SeatingDetails("test_section2", 20, 30, true));

        var startTime1 = ZonedDateTime.now(ZoneId.of("UTC")).minus(Duration.ofDays(1));
        var startTime2 = ZonedDateTime.now(ZoneId.of("UTC")).minus(Duration.ofDays(2));
        var startTime3 = ZonedDateTime.now(ZoneId.of("UTC")).minus(Duration.ofDays(3));
        var endTime1 = startTime1.plus(Duration.ofHours(1));
        var endTime2 = startTime2.plus(Duration.ofHours(1));
        var endTime3 = startTime3.plus(Duration.ofHours(1));

        response = httpHelper.post("/api/test/event/create", new CreateEventReqBuilder()
                .withEventName("Test Event")
                .withSeatingDetails(seatingDetails)
                .withStartDate(startTime1.minusMinutes(2))
                .withEndDate(endTime1)
                .build(authToken));
        assertEquals(200, response.getStatus());
        eventId1 = response.getBody(CreateEventResponse.class).event_id;

        response = httpHelper.put("/api/event/edit", new EditEventRequest(eventId1, authToken, null, null, null, null, null, null, null, null, null, null, true, null));
        assertEquals(200, response.getStatus());

        response = httpHelper.post("/api/test/event/create", new CreateEventReqBuilder()
                .withEventName("Test Event")
                .withSeatingDetails(seatingDetails)
                .withStartDate(startTime2.minusMinutes(2))
                .withEndDate(endTime2)
                .build(authToken));
        assertEquals(200, response.getStatus());
        eventId2 = response.getBody(CreateEventResponse.class).event_id;

        response = httpHelper.put("/api/event/edit", new EditEventRequest(eventId2, authToken, null, null, null, null, null, null, null, null, null, null, true, null));
        assertEquals(200, response.getStatus());

        response = httpHelper.post("/api/test/event/create", new CreateEventReqBuilder()
                .withEventName("Test Event")
                .withSeatingDetails(seatingDetails)
                .withStartDate(startTime3.minusMinutes(2))
                .withEndDate(endTime3)
                .build(authToken));
        assertEquals(200, response.getStatus());
        eventId3 = response.getBody(CreateEventResponse.class).event_id;

        response = httpHelper.put("/api/event/edit", new EditEventRequest(eventId3, authToken, null, null, null, null, null, null, null, null, null, null, true, null));
        assertEquals(200, response.getStatus());

      
    }

    @AfterEach
    public void cleanup () {
        Spark.stop();
        hibernateModel.cleanup();
        Spark.awaitStop();
        ApiLocator.clearLocator(ILocationAPI.class);
    }

    @Test 
    public void testEventHostingsPast () {
        int pageStart = 0;
        int maxResults = 10;
        var response = httpHelper.get("/api/event/hosting/past", Map.of(
            "auth_token", authToken, 
            "page_start", Integer.toString(pageStart), 
            "max_results", Integer.toString(maxResults)
        ));
        assertEquals(200, response.getStatus()); 
        var bookings = response.getBody(EventHostingsResponse.class);
        assertEquals(3, bookings.eventIds.size());
        assertEquals(eventId3, bookings.eventIds.get(0));
        assertEquals(eventId2, bookings.eventIds.get(1));
        assertEquals(eventId1, bookings.eventIds.get(2));
        assertEquals(3, bookings.numResults);


        response = httpHelper.get("/api/event/hosting/past", Map.of(
            "auth_token", authToken, 
            "page_start", Integer.toString(pageStart), 
            "max_results", Integer.toString(2)
        ));
        assertEquals(200, response.getStatus()); 
        bookings = response.getBody(EventHostingsResponse.class);
        assertEquals(2, bookings.eventIds.size());
        assertEquals(3, bookings.numResults);
        assertEquals(bookings.eventIds.get(0), eventId3);
        assertEquals(bookings.eventIds.get(1), eventId2);
    }

    @Test 
    public void testEventHostingsPastAfter() {
        int pageStart = 0;
        int maxResults = 10;
        var response = httpHelper.get("/api/event/hosting/past", Map.of(
            "auth_token", authToken, 
            "page_start", Integer.toString(pageStart), 
            "max_results", Integer.toString(maxResults),
            "after", ZonedDateTime.now(ZoneId.of("UTC")).minus(Duration.ofDays(3)).toString()
        ));
        assertEquals(200, response.getStatus()); 
        var bookings = response.getBody(EventHostingsResponse.class);
        assertEquals(2, bookings.eventIds.size());
        assertEquals(bookings.eventIds.get(0), eventId2);
        assertEquals(bookings.eventIds.get(1), eventId1);
        assertEquals(2, bookings.numResults);
    }

    @Test 
    public void testExceptions () {
        int numEvents = 16; 
        int pageStart = 0;
        var response = httpHelper.get("/api/event/hosting/past", Map.of(
            "page_start", Integer.toString(pageStart), 
            "max_results", Integer.toString(numEvents)
        ));
        assertEquals(400, response.getStatus());
        response = httpHelper.get("/api/event/hosting/past", Map.of(
            "auth_token", authToken, 
            "max_results", Integer.toString(numEvents)
        ));
        assertEquals(400, response.getStatus());
        response = httpHelper.get("/api/event/hosting/past", Map.of(
            "auth_token", authToken, 
            "page_start", Integer.toString(pageStart)
        ));
        assertEquals(400, response.getStatus());
        response = httpHelper.get("/api/event/hosting/past", Map.of(
            "auth_token", "authToken", 
            "page_start", Integer.toString(pageStart), 
            "max_results", Integer.toString(numEvents)
        ));
        assertEquals(401, response.getStatus());
        response = httpHelper.get("/api/event/hosting/past", Map.of(
            "auth_token", authToken, 
            "page_start", Integer.toString(-1), 
            "max_results", Integer.toString(numEvents)
        ));
        assertEquals(400, response.getStatus());
        response = httpHelper.get("/api/event/hosting/past", Map.of(
            "auth_token", authToken, 
            "page_start", Integer.toString(pageStart), 
            "max_results", Integer.toString(-1)
        ));
        assertEquals(400, response.getStatus());
        response = httpHelper.get("/api/event/hosting/past", Map.of(
            "auth_token", authToken, 
            "page_start", Integer.toString(pageStart), 
            "max_results", Integer.toString(10),
            "after", " "
        ));
        assertEquals(400, response.getStatus());
    }
}
