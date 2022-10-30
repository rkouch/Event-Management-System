package tickr.unit.user;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.stripe.model.Event;

import static org.junit.jupiter.api.Assertions.*;
import tickr.CreateEventReqBuilder;
import tickr.TestHelper;
import tickr.application.TickrController;
import tickr.application.apis.ApiLocator;
import tickr.application.apis.purchase.IPurchaseAPI;
import tickr.application.entities.PurchaseItem;
import tickr.application.entities.SeatingPlan;
import tickr.application.entities.Ticket;
import tickr.application.entities.TicketReservation;
import tickr.application.serialised.combined.TicketPurchase;
import tickr.application.serialised.combined.TicketReserve;
import tickr.application.serialised.requests.CreateEventRequest;
import tickr.application.serialised.requests.EditEventRequest;
import tickr.application.serialised.requests.UserRegisterRequest;
import tickr.application.serialised.responses.EventAttendeesResponse;
import tickr.mock.AbstractMockPurchaseAPI;
import tickr.mock.MockUnitPurchaseAPI;
import tickr.persistence.DataModel;
import tickr.persistence.HibernateModel;
import tickr.persistence.ModelSession;
import tickr.server.exceptions.BadRequestException;
import tickr.server.exceptions.ForbiddenException;
import tickr.server.exceptions.UnauthorizedException;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class TestCustomerBookings {
    private DataModel model;
    private TickrController controller;
    private ModelSession session;
    private AbstractMockPurchaseAPI purchaseAPI;

    private String eventId;
    private String eventId2;
    private String eventId3;
    private String authToken; 
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    private List<String> requestIds;
    private float requestPrice;

    @BeforeEach
    public void setup () {
        model = new HibernateModel("hibernate-test.cfg.xml");
        controller = new TickrController();

        purchaseAPI = new MockUnitPurchaseAPI(controller, model);
        ApiLocator.addLocator(IPurchaseAPI.class, () -> purchaseAPI);

        List<CreateEventRequest.SeatingDetails> seatingDetails = new ArrayList<>();
        seatingDetails.add(new CreateEventRequest.SeatingDetails("SectionA", 10, 50, true));
        seatingDetails.add(new CreateEventRequest.SeatingDetails("SectionB", 20, 30, true));

        session = model.makeSession();

        authToken = controller.userRegister(session,
        new UserRegisterRequest("test", "first", "last", "test1@example.com",
                "Password123!", "2022-04-14")).authToken;

        session = TestHelper.commitMakeSession(model, session);

        startTime = LocalDateTime.now(ZoneId.of("UTC")).plus(Duration.ofDays(1));
        var startTime2 = LocalDateTime.now(ZoneId.of("UTC")).plus(Duration.ofDays(2));
        var startTime3 = LocalDateTime.now(ZoneId.of("UTC")).plus(Duration.ofDays(3));
        endTime = startTime.plus(Duration.ofHours(1));
        var endTime2 = startTime2.plus(Duration.ofHours(1));
        var endTime3 = startTime3.plus(Duration.ofHours(1));
        
        eventId = controller.createEvent(session, new CreateEventReqBuilder()
                .withEventName("Test Event1")
                .withSeatingDetails(seatingDetails)
                .withStartDate(startTime.minusMinutes(2))
                .withEndDate(endTime)
                .build(authToken)).event_id;

        controller.editEvent(session, new EditEventRequest(eventId, authToken, null, null, null, null, null, null, null, null, null, null, true));

        session = TestHelper.commitMakeSession(model, session);
        
        eventId2 = controller.createEvent(session, new CreateEventReqBuilder()
                .withEventName("Test Event2")
                .withSeatingDetails(seatingDetails)
                .withStartDate(startTime2.minusMinutes(2))
                .withEndDate(endTime2)
                .build(authToken)).event_id;
        
        controller.editEvent(session, new EditEventRequest(eventId2, authToken, null, null, null, null, null, null, null, null, null, null, true));

        session = TestHelper.commitMakeSession(model, session);
        
        eventId3 = controller.createEvent(session, new CreateEventReqBuilder()
                .withEventName("Test Event3")
                .withSeatingDetails(seatingDetails)
                .withStartDate(startTime3.minusMinutes(2))
                .withEndDate(endTime3)
                .build(authToken)).event_id;
        
        session = TestHelper.commitMakeSession(model, session);

        controller.editEvent(session, new EditEventRequest(eventId3, authToken, null, null, null, null, null, null, null, null, null, null, true));

        var response = controller.ticketReserve(session, new TicketReserve.Request(authToken, eventId, startTime, List.of(
                new TicketReserve.TicketDetails("SectionA", 1, List.of(1)),
                new TicketReserve.TicketDetails("SectionB", 1, List.of(2))
        )));
        requestIds = response.reserveTickets.stream().map(t -> t.reserveId).collect(Collectors.toList());
        requestPrice = response.reserveTickets.stream().map(t -> t.price).reduce(0.0f, Float::sum);

        var reqIds = requestIds.stream().map(TicketPurchase.TicketDetails::new).collect(Collectors.toList());
        purchaseAPI.addCustomer("test_customer", 1000);
        var redirectUrl = controller.ticketPurchase(session, new TicketPurchase.Request(authToken,
                "https://example.com/success", "https://example.com/cancel", reqIds)).redirectUrl;
        session = TestHelper.commitMakeSession(model, session);
        purchaseAPI.fulfillOrder(redirectUrl, "test_customer");

        response = controller.ticketReserve(session, new TicketReserve.Request(authToken, eventId3, startTime3, List.of(
                new TicketReserve.TicketDetails("SectionA", 2, List.of(1, 2)),
                new TicketReserve.TicketDetails("SectionB", 2, List.of(2, 3))
        )));
        requestIds = response.reserveTickets.stream().map(t -> t.reserveId).collect(Collectors.toList());
        requestPrice = response.reserveTickets.stream().map(t -> t.price).reduce(0.0f, Float::sum);

        reqIds = requestIds.stream().map(TicketPurchase.TicketDetails::new).collect(Collectors.toList());
        purchaseAPI.addCustomer("test_customer", 1000);
        redirectUrl = controller.ticketPurchase(session, new TicketPurchase.Request(authToken,
                "https://example.com/success", "https://example.com/cancel", reqIds)).redirectUrl;
        session = TestHelper.commitMakeSession(model, session);
        purchaseAPI.fulfillOrder(redirectUrl, "test_customer");

        response = controller.ticketReserve(session, new TicketReserve.Request(authToken, eventId2, startTime2, List.of(
                new TicketReserve.TicketDetails("SectionA", 1, List.of(1)),
                new TicketReserve.TicketDetails("SectionB", 2, List.of(2, 3))
        )));
        requestIds = response.reserveTickets.stream().map(t -> t.reserveId).collect(Collectors.toList());
        requestPrice = response.reserveTickets.stream().map(t -> t.price).reduce(0.0f, Float::sum);

        reqIds = requestIds.stream().map(TicketPurchase.TicketDetails::new).collect(Collectors.toList());
        purchaseAPI.addCustomer("test_customer", 1000);
        redirectUrl = controller.ticketPurchase(session, new TicketPurchase.Request(authToken,
                "https://example.com/success", "https://example.com/cancel", reqIds)).redirectUrl;
        session = TestHelper.commitMakeSession(model, session);
        purchaseAPI.fulfillOrder(redirectUrl, "test_customer");
    }

    @AfterEach
    public void cleanup () {
        model.cleanup();
    }

    @Test 
    public void testCustomerBookings () {
        int pageStart = 0;
        int maxResults = 10;
        var bookings = controller.customerBookings(session, Map.of(
            "auth_token", authToken, 
            "page_start", Integer.toString(pageStart), 
            "max_results", Integer.toString(maxResults)
        )).bookings;
        assertEquals(3, bookings.size());
        assertEquals(bookings.get(0).eventId, eventId);
        assertEquals(bookings.get(1).eventId, eventId2);
        assertEquals(bookings.get(2).eventId, eventId3);
        var event1Ids = bookings.get(0).ticketIds; 
        var event2Ids = bookings.get(1).ticketIds; 
        var event3Ids = bookings.get(2).ticketIds; 
        assertEquals(2, event1Ids.size());
        assertEquals(3, event2Ids.size());
        assertEquals(4, event3Ids.size());

        bookings = controller.customerBookings(session, Map.of(
            "auth_token", authToken, 
            "page_start", Integer.toString(pageStart), 
            "max_results", Integer.toString(2)
        )).bookings;
        assertEquals(2, bookings.size());
        assertEquals(bookings.get(0).eventId, eventId);
        assertEquals(bookings.get(1).eventId, eventId2);
    }

    @Test 
    public void testExceptions() {
        int pageStart = 0;
        int maxResults = 10;
        assertThrows(BadRequestException.class, () -> controller.customerBookings(session, Map.of(
            "page_start", Integer.toString(pageStart), 
            "max_results", Integer.toString(maxResults)
        )));
        assertThrows(BadRequestException.class, () -> controller.customerBookings(session, Map.of(
            "auth_token", authToken, 
            "max_results", Integer.toString(maxResults)
        )));
        assertThrows(BadRequestException.class, () -> controller.customerBookings(session, Map.of(
            "auth_token", authToken, 
            "page_start", Integer.toString(pageStart)
        )));
        assertThrows(BadRequestException.class, () -> controller.customerBookings(session, Map.of(
            "auth_token", authToken, 
            "page_start", Integer.toString(-1), 
            "max_results", Integer.toString(maxResults)
        )));
        assertThrows(BadRequestException.class, () -> controller.customerBookings(session, Map.of(
            "auth_token", authToken, 
            "page_start", Integer.toString(pageStart), 
            "max_results", Integer.toString(-1)
        )));
    }
}
