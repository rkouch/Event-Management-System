package tickr.integration.ticket;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import spark.Spark;
import tickr.CreateEventReqBuilder;
import tickr.TestHelper;
import tickr.application.apis.ApiLocator;
import tickr.application.apis.purchase.IPurchaseAPI;
import tickr.application.serialised.combined.TicketReserve;
import tickr.application.serialised.requests.CreateEventRequest;
import tickr.application.serialised.requests.UserRegisterRequest;
import tickr.application.serialised.responses.AuthTokenResponse;
import tickr.application.serialised.responses.CreateEventResponse;
import tickr.persistence.DataModel;
import tickr.persistence.HibernateModel;
import tickr.server.Server;
import tickr.util.HTTPHelper;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class TestTicketReserve {
    private DataModel hibernateModel;
    private HTTPHelper httpHelper;
    private String authToken;

    private String eventId;

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    private List<CreateEventRequest.SeatingDetails> seatingDetails;

    @BeforeEach
    public void setup () {
        hibernateModel = new HibernateModel("hibernate-test.cfg.xml");

        Server.start(8080, null, hibernateModel);
        httpHelper = new HTTPHelper("http://localhost:8080");
        Spark.awaitInitialization();

        var response = httpHelper.post("/api/user/register", new UserRegisterRequest("TestUsername", "Test", "User", "test@example.com",
                "Password123!", "2010-10-07"));
        assertEquals(200, response.getStatus());
        authToken = response.getBody(AuthTokenResponse.class).authToken;

        startTime = LocalDateTime.now(ZoneId.of("UTC")).plus(Duration.ofDays(1));
        endTime = startTime.plus(Duration.ofHours(1));

        seatingDetails = List.of(
                new CreateEventRequest.SeatingDetails("test_section", 10, 1),
                new CreateEventRequest.SeatingDetails("test_section2", 20, 4)
        );

        response = httpHelper.post("/api/event/create", new CreateEventReqBuilder()
                .withStartDate(startTime.minusMinutes(2))
                .withEndDate(endTime)
                .withSeatingDetails(seatingDetails)
                .build(authToken));
        assertEquals(200, response.getStatus());
        eventId = response.getBody(CreateEventResponse.class).event_id;
    }

    @AfterEach
    public void cleanup () {
        Spark.stop();
        hibernateModel.cleanup();
        Spark.awaitStop();

        ApiLocator.clearLocator(IPurchaseAPI.class);
    }

    @Test
    public void testBadRequests () {
        var ticketDetails = List.of(new TicketReserve.TicketDetails("test_section"));
        var response = httpHelper.post("/api/ticket/reserve",
                new TicketReserve.Request(authToken, null, (String)null, null));
        assertEquals(400, response.getStatus());

        response = httpHelper.post("/api/ticket/reserve",
                new TicketReserve.Request(TestHelper.makeFakeJWT(), eventId, startTime, ticketDetails));
        assertEquals(401, response.getStatus());

        response = httpHelper.post("/api/ticket/reserve",
                new TicketReserve.Request(authToken, UUID.randomUUID().toString(), startTime, ticketDetails));
        assertEquals(403, response.getStatus());

        response = httpHelper.post("/api/ticket/reserve",
                new TicketReserve.Request(authToken, eventId, "abcdefg", ticketDetails));
        assertEquals(400, response.getStatus());

        response = httpHelper.post("/api/ticket/reserve",
                new TicketReserve.Request(authToken, eventId, startTime, List.of()));
        assertEquals(400, response.getStatus());
    }

    @Test
    public void testOneTicket () {
        var ticketDetails = List.of(new TicketReserve.TicketDetails("test_section"));

        var response = httpHelper.post("/api/ticket/reserve", new TicketReserve.Request(authToken, eventId, startTime, ticketDetails));
        assertEquals(200, response.getStatus());
        assertEquals(1, Float.parseFloat(response.getBody(TicketReserve.Response.class).price));
    }

    @Test
    public void testMultipleTickets () {
        var ticketDetails = List.of(
                new TicketReserve.TicketDetails( null, null, null, "test_section", 4),
                new TicketReserve.TicketDetails( null, null, null, "test_section", 5),
                new TicketReserve.TicketDetails( null, null, null, "test_section", 6),
                new TicketReserve.TicketDetails( null, null, null, "test_section", 7)
        );

        var response = httpHelper.post("/api/ticket/reserve", new TicketReserve.Request(authToken, eventId, startTime, ticketDetails));
        assertEquals(200, response.getStatus());
        assertEquals(4, Float.parseFloat(response.getBody(TicketReserve.Response.class).price));
    }

    @Test
    public void testMultipleSections () {
        var ticketDetails = List.of(
                new TicketReserve.TicketDetails( null, null, null, "test_section", 4),
                new TicketReserve.TicketDetails(null, null, null, "test_section2", 5)
        );

        var response = httpHelper.post("/api/ticket/reserve", new TicketReserve.Request(authToken, eventId, startTime, ticketDetails));
        assertEquals(200, response.getStatus());
        assertEquals(5, Float.parseFloat(response.getBody(TicketReserve.Response.class).price));
    }
}