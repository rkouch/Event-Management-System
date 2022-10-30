package tickr.integration.user;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
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
import tickr.application.apis.purchase.IPurchaseAPI;
import tickr.application.serialised.combined.TicketPurchase;
import tickr.application.serialised.combined.TicketReserve;
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
import tickr.mock.MockHttpPurchaseAPI;
import tickr.persistence.DataModel;
import tickr.persistence.HibernateModel;
import tickr.server.Server;
import tickr.util.HTTPHelper;
public class TestCustomerBookings {
    private DataModel hibernateModel;
    private HTTPHelper httpHelper;
    private MockHttpPurchaseAPI purchaseAPI;

    private String authToken; 
    private String eventId; 
    private String eventId2;
    private String eventId3;

    private List<String> requestIds;
    private float requestPrice;


    @BeforeEach
    public void setup () {
        // hibernateModel = new HibernateModel("hibernate-test.cfg.xml");
        // purchaseAPI = new MockHttpPurchaseAPI("http://localhost:8080");
        // ApiLocator.addLocator(IPurchaseAPI.class, () -> purchaseAPI);

        // Server.start(8080, null, hibernateModel);
        // httpHelper = new HTTPHelper("http://localhost:8080");
        // Spark.awaitInitialization();

        // var response = httpHelper.post("/api/user/register", new UserRegisterRequest("test", "first", "last", "test1@example.com",
        //         "Password123!", "2022-04-14"));
        // assertEquals(200, response.getStatus());
        // authToken = response.getBody(AuthTokenResponse.class).authToken;

        // List<CreateEventRequest.SeatingDetails> seatingDetails = new ArrayList<>();
        // seatingDetails.add(new CreateEventRequest.SeatingDetails("test_section", 10, 50, true));
        // seatingDetails.add(new CreateEventRequest.SeatingDetails("test_section2", 20, 30, true));

        // var startTime1 = LocalDateTime.now().plusDays(1);
        // var startTime2 = LocalDateTime.now().plusDays(3);
        // var startTime3 = LocalDateTime.now().plusDays(6);
        // var endTime1 = LocalDateTime.now().plusDays(2);
        // var endTime2 = LocalDateTime.now().plusDays(4);
        // var endTime3 = LocalDateTime.now().plusDays(7);

        // response = httpHelper.post("/api/event/create", new CreateEventReqBuilder()
        //         .withEventName("Test Event")
        //         .withSeatingDetails(seatingDetails)
        //         .withStartDate(startTime1)
        //         .withEndDate(endTime1)
        //         .build(authToken));
        // assertEquals(200, response.getStatus());
        // eventId = response.getBody(CreateEventResponse.class).event_id;

        // response = httpHelper.post("/api/event/create", new CreateEventReqBuilder()
        //         .withEventName("Test Event")
        //         .withSeatingDetails(seatingDetails)
        //         .withStartDate(startTime2)
        //         .withEndDate(endTime2)
        //         .build(authToken));
        // assertEquals(200, response.getStatus());
        // eventId2 = response.getBody(CreateEventResponse.class).event_id;

        // response = httpHelper.post("/api/event/create", new CreateEventReqBuilder()
        //         .withEventName("Test Event")
        //         .withSeatingDetails(seatingDetails)
        //         .withStartDate(startTime3)
        //         .withEndDate(endTime3)
        //         .build(authToken));
        // assertEquals(200, response.getStatus());
        // eventId3 = response.getBody(CreateEventResponse.class).event_id;

        // response = httpHelper.post("/api/ticket/reserve", new TicketReserve.Request(authToken, eventId, startTime1, List.of(
        //         new TicketReserve.TicketDetails("test_section", 1, List.of(1)),
        //         new TicketReserve.TicketDetails("test_section2", 1, List.of(2))
        // )));
        // assertEquals(200, response.getStatus());
        // var reserveResponse = response.getBody(TicketReserve.Response.class);
        // requestIds = reserveResponse.reserveTickets.stream()
        //         .map(t -> t.reserveId)
        //         .collect(Collectors.toList());
        // requestPrice = reserveResponse.reserveTickets.stream()
        //         .map(t -> t.price)
        //         .reduce(0.0f, Float::sum);

        // var requestDetails = requestIds.stream()        
        //         .map(TicketPurchase.TicketDetails::new)
        //         .collect(Collectors.toList());
        // purchaseAPI.addCustomer("test_customer", 1000);
        // response = httpHelper.post("/api/ticket/purchase",
        //         new TicketPurchase.Request(authToken, "https://example.com/success", "https://example.com/cancel", requestDetails));
        // assertEquals(200, response.getStatus());
        // var redirectUrl = response.getBody(TicketPurchase.Response.class).redirectUrl;
        // assertTrue(purchaseAPI.isUrlValid(redirectUrl));
        // var result = purchaseAPI.fulfillOrder(redirectUrl, "test_customer");
        // assertEquals("https://example.com/success", result);


        // response = httpHelper.post("/api/ticket/reserve", new TicketReserve.Request(authToken, eventId2, startTime2, List.of(
        //         new TicketReserve.TicketDetails("test_section", 2, List.of(3, 4)),
        //         new TicketReserve.TicketDetails("test_section2", 1, List.of(5))
        // )));
        // assertEquals(200, response.getStatus());
        // reserveResponse = response.getBody(TicketReserve.Response.class);
        // requestIds = reserveResponse.reserveTickets.stream()
        //         .map(t -> t.reserveId)
        //         .collect(Collectors.toList());
        // requestPrice = reserveResponse.reserveTickets.stream()
        //         .map(t -> t.price)
        //         .reduce(0.0f, Float::sum);

        // requestDetails = requestIds.stream()        
        //         .map(TicketPurchase.TicketDetails::new)
        //         .collect(Collectors.toList());
        // purchaseAPI.addCustomer("test_customer", 1000);
        // response = httpHelper.post("/api/ticket/purchase",
        //         new TicketPurchase.Request(authToken, "https://example.com/success", "https://example.com/cancel", requestDetails));
        // assertEquals(200, response.getStatus());
        // redirectUrl = response.getBody(TicketPurchase.Response.class).redirectUrl;
        // assertTrue(purchaseAPI.isUrlValid(redirectUrl));
        // result = purchaseAPI.fulfillOrder(redirectUrl, "test_customer");
        // assertEquals("https://example.com/success", result);



        // response = httpHelper.post("/api/ticket/reserve", new TicketReserve.Request(authToken, eventId3, startTime3, List.of(
        //         new TicketReserve.TicketDetails("test_section", 2, List.of(6, 7)),
        //         new TicketReserve.TicketDetails("test_section2", 2, List.of(8, 9))
        // )));
        // assertEquals(200, response.getStatus());
        // reserveResponse = response.getBody(TicketReserve.Response.class);
        // requestIds = reserveResponse.reserveTickets.stream()
        //         .map(t -> t.reserveId)
        //         .collect(Collectors.toList());
        // requestPrice = reserveResponse.reserveTickets.stream()
        //         .map(t -> t.price)
        //         .reduce(0.0f, Float::sum);

        // requestDetails = requestIds.stream()        
        //         .map(TicketPurchase.TicketDetails::new)
        //         .collect(Collectors.toList());
        // purchaseAPI.addCustomer("test_customer", 1000);
        // response = httpHelper.post("/api/ticket/purchase",
        //         new TicketPurchase.Request(authToken, "https://example.com/success", "https://example.com/cancel", requestDetails));
        // assertEquals(200, response.getStatus());
        // redirectUrl = response.getBody(TicketPurchase.Response.class).redirectUrl;
        // assertTrue(purchaseAPI.isUrlValid(redirectUrl));
        // result = purchaseAPI.fulfillOrder(redirectUrl, "test_customer");
        // assertEquals("https://example.com/success", result);
    }

    @AfterEach
    public void cleanup () {
        Spark.stop();
        hibernateModel.cleanup();
        Spark.awaitStop();
    }
}
