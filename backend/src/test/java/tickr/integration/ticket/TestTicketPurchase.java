package tickr.integration.ticket;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import spark.Spark;
import tickr.CreateEventReqBuilder;
import tickr.TestHelper;
import tickr.application.apis.ApiLocator;
import tickr.application.apis.purchase.IPurchaseAPI;
import tickr.application.serialised.combined.TicketPurchase;
import tickr.application.serialised.combined.TicketReserve;
import tickr.application.serialised.requests.CreateEventRequest;
import tickr.application.serialised.requests.EditEventRequest;
import tickr.application.serialised.requests.UserRegisterRequest;
import tickr.application.serialised.responses.AuthTokenResponse;
import tickr.application.serialised.responses.CreateEventResponse;
import tickr.mock.MockHttpPurchaseAPI;
import tickr.persistence.DataModel;
import tickr.persistence.HibernateModel;
import tickr.server.Server;
import tickr.util.HTTPHelper;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestTicketPurchase {
    private DataModel hibernateModel;
    private HTTPHelper httpHelper;

    private MockHttpPurchaseAPI purchaseAPI;

    private String authToken;

    private String eventId;

    private String requestId;
    private List<String> requestIds;
    private float requestPrice;

    private ZonedDateTime startTime;
    private ZonedDateTime endTime;

    @BeforeEach
    public void setup () {
        hibernateModel = new HibernateModel("hibernate-test.cfg.xml");
        purchaseAPI = new MockHttpPurchaseAPI("http://localhost:8080");
        ApiLocator.addLocator(IPurchaseAPI.class, () -> purchaseAPI);

        Server.start(8080, null, hibernateModel);
        httpHelper = new HTTPHelper("http://localhost:8080");
        Spark.awaitInitialization();

        var response = httpHelper.post("/api/user/register", new UserRegisterRequest("TestUsername", "Test", "User", "test@example.com",
                "Password123!", "2010-10-07"));
        assertEquals(200, response.getStatus());
        authToken = response.getBody(AuthTokenResponse.class).authToken;

        startTime = ZonedDateTime.now(ZoneId.of("UTC")).plus(Duration.ofDays(1));
        endTime = startTime.plus(Duration.ofHours(1));

        List<CreateEventRequest.SeatingDetails> seatingDetails = List.of(
                new CreateEventRequest.SeatingDetails("test_section", 10, 1, true),
                new CreateEventRequest.SeatingDetails("test_section2", 20, 4, true)
        );

        response = httpHelper.post("/api/event/create", new CreateEventReqBuilder()
                .withStartDate(startTime.minusMinutes(2))
                .withEndDate(endTime)
                .withSeatingDetails(seatingDetails)
                .build(authToken));
        assertEquals(200, response.getStatus());
        eventId = response.getBody(CreateEventResponse.class).event_id;

        response = httpHelper.put("/api/event/edit", new EditEventRequest(eventId, authToken, null, null, null, null,
                null, null, null, null, null, null, true));
        assertEquals(200, response.getStatus());

        response = httpHelper.post("/api/ticket/reserve", new TicketReserve.Request(authToken, eventId, startTime, List.of(
                new TicketReserve.TicketDetails("test_section", 1, List.of()),
                new TicketReserve.TicketDetails("test_section2", 1, List.of())
        )));
        assertEquals(200, response.getStatus());
        var reserveResponse = response.getBody(TicketReserve.Response.class);
        requestIds = reserveResponse.reserveTickets.stream()
                .map(t -> t.reserveId)
                .collect(Collectors.toList());
        requestPrice = reserveResponse.reserveTickets.stream()
                .map(t -> t.price)
                .reduce(0.0f, Float::sum);
    }

    @AfterEach
    public void cleanup () {
        Spark.stop();
        hibernateModel.cleanup();
        Spark.awaitStop();

        ApiLocator.clearLocator(IPurchaseAPI.class);
    }

    @Test
    public void testBadRequest () {
        var requestDetails = requestIds.stream()
                .map(TicketPurchase.TicketDetails::new)
                .collect(Collectors.toList());
        var response = httpHelper.post("/api/ticket/purchase",
                new TicketPurchase.Request(authToken, null, null, null));
        assertEquals(400, response.getStatus());

        response = httpHelper.post("/api/ticket/purchase", new TicketPurchase.Request(TestHelper.makeFakeJWT(), "testing", "testing", requestDetails));
        assertEquals(401, response.getStatus());

        response = httpHelper.post("/api/ticket/purchase", new TicketPurchase.Request(authToken, "http://testing.com", "http://testing.com",
                List.of(new TicketPurchase.TicketDetails(UUID.randomUUID().toString()))));
        assertEquals(403, response.getStatus());

        response = httpHelper.post("/api/user/register", TestHelper.makeRegisterRequest());
        assertEquals(200, response.getStatus());
        var newUser = response.getBody(AuthTokenResponse.class).authToken;

        response = httpHelper.post("/api/ticket/purchase",
                new TicketPurchase.Request(newUser, "http://testing.com", "http://testing.com", requestDetails));
        assertEquals(403, response.getStatus());
    }

    @Test
    public void testPurchase () {
        var requestDetails = requestIds.stream()
                .map(TicketPurchase.TicketDetails::new)
                .collect(Collectors.toList());
        purchaseAPI.addCustomer("test_customer", 10);
        var response = httpHelper.post("/api/ticket/purchase",
                new TicketPurchase.Request(authToken, "https://example.com/success", "https://example.com/cancel", requestDetails));
        assertEquals(200, response.getStatus());

        var redirectUrl = response.getBody(TicketPurchase.Response.class).redirectUrl;
        assertTrue(purchaseAPI.isUrlValid(redirectUrl));

        var result = purchaseAPI.fulfillOrder(redirectUrl, "test_customer");
        assertEquals("https://example.com/success", result);

        assertEquals(10 - requestPrice, purchaseAPI.getCustomer("test_customer").getBalance());
        assertTrue(purchaseAPI.hasReceivedWebhook());

        response = httpHelper.post("/api/ticket/purchase",
                new TicketPurchase.Request(authToken, "https://example.com/success", "https://example.com/cancel", requestDetails));
        assertEquals(403, response.getStatus());
    }

    @Test
    public void testCancel () {
        var requestDetails = requestIds.stream()
                .map(TicketPurchase.TicketDetails::new)
                .collect(Collectors.toList());
        purchaseAPI.addCustomer("test_customer", 10);
        var response = httpHelper.post("/api/ticket/purchase",
                new TicketPurchase.Request(authToken, "https://example.com/success", "https://example.com/cancel", requestDetails));
        assertEquals(200, response.getStatus());

        var redirectUrl = response.getBody(TicketPurchase.Response.class).redirectUrl;
        assertTrue(purchaseAPI.isUrlValid(redirectUrl));

        var result = purchaseAPI.cancelOrder(redirectUrl);
        assertEquals("https://example.com/cancel", result);

        assertTrue(purchaseAPI.hasReceivedWebhook());

        response = httpHelper.post("/api/ticket/purchase",
                new TicketPurchase.Request(authToken, "https://example.com/success", "https://example.com/cancel", requestDetails));
        assertEquals(403, response.getStatus());
    }

    @Test
    public void testLowBalance () {
        var requestDetails = requestIds.stream()
                .map(TicketPurchase.TicketDetails::new)
                .collect(Collectors.toList());
        purchaseAPI.addCustomer("test_customer", 0.5f);
        var response = httpHelper.post("/api/ticket/purchase",
                new TicketPurchase.Request(authToken, "https://example.com/success", "https://example.com/cancel", requestDetails));
        assertEquals(200, response.getStatus());

        var redirectUrl = response.getBody(TicketPurchase.Response.class).redirectUrl;
        assertTrue(purchaseAPI.isUrlValid(redirectUrl));

        var result = purchaseAPI.fulfillOrder(redirectUrl, "test_customer");
        assertEquals("https://example.com/cancel", result);

        assertTrue(purchaseAPI.hasReceivedWebhook());

        response = httpHelper.post("/api/ticket/purchase",
                new TicketPurchase.Request(authToken, "https://example.com/success", "https://example.com/cancel", requestDetails));
        assertEquals(403, response.getStatus());
    }
}
