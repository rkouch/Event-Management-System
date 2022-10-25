package tickr.unit.ticket;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.google.api.client.util.DateTime;
import com.stripe.model.Event;

import tickr.CreateEventReqBuilder;
import tickr.TestHelper;
import tickr.application.TickrController;
import tickr.application.apis.ApiLocator;
import tickr.application.apis.purchase.IPurchaseAPI;
import tickr.application.entities.User;
import tickr.application.serialised.SerializedLocation;
import tickr.application.serialised.combined.TicketPurchase;
import tickr.application.serialised.combined.TicketReserve;
import tickr.application.serialised.requests.CreateEventRequest;
import tickr.application.serialised.requests.UserRegisterRequest;
import tickr.mock.AbstractMockPurchaseAPI;
import tickr.mock.MockUnitPurchaseAPI;
import tickr.persistence.DataModel;
import tickr.persistence.HibernateModel;
import tickr.persistence.ModelSession;
import tickr.server.exceptions.BadRequestException;
import tickr.server.exceptions.ForbiddenException;
import tickr.server.exceptions.UnauthorizedException;
import tickr.util.CryptoHelper;

public class TestTicketView {
    private DataModel model;
    private TickrController controller;
    private ModelSession session;
    private AbstractMockPurchaseAPI purchaseAPI;

    private String eventId;
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

        startTime = LocalDateTime.now(ZoneId.of("UTC")).plus(Duration.ofDays(1));
        endTime = startTime.plus(Duration.ofHours(1));
        
        eventId = controller.createEvent(session, new CreateEventReqBuilder()
            .withEventName("Test Event")
            .withSeatingDetails(seatingDetails)
            .withStartDate(startTime.minusMinutes(2))
            .withEndDate(endTime)
            .build(authToken)).event_id;
            
        
        session = TestHelper.commitMakeSession(model, session);

        var response = controller.ticketReserve(session, new TicketReserve.Request(authToken, eventId, startTime, List.of(
                new TicketReserve.TicketDetails("SectionA", 1, List.of(1)),
                new TicketReserve.TicketDetails("SectionB", 1, List.of(2))
        )));
        requestIds = response.reserveTickets.stream().map(t -> t.reserveId).collect(Collectors.toList());
        requestPrice = response.reserveTickets.stream().map(t -> t.price).reduce(0.0f, Float::sum);

        var reqIds = requestIds.stream().map(TicketPurchase.TicketDetails::new).collect(Collectors.toList());
        purchaseAPI.addCustomer("test_customer", 100);
        var redirectUrl = controller.ticketPurchase(session, new TicketPurchase.Request(authToken,
                "https://example.com/success", "https://example.com/cancel", reqIds)).redirectUrl;
        session = TestHelper.commitMakeSession(model, session);
        purchaseAPI.fulfillOrder(redirectUrl, "test_customer");

    }

    @AfterEach
    public void cleanup () {
        model.cleanup();
    }

    @Test 
    public void testTicketBookings () {
        // Event event = session.getById(Event.class, eventId).orElse(null);
        Set<String> ticketIds = controller.ticketBookings(session, Map.of("event_id", eventId, "auth_token", authToken)).tickets;
        assertTrue(ticketIds.size() == 2);

        var authToken2 = controller.userRegister(session,
        new UserRegisterRequest("test", "first", "last", "test2@example.com",
                "Password123!", "2022-04-14")).authToken;

        var response = controller.ticketReserve(session, new TicketReserve.Request(authToken2, eventId, startTime, List.of(
                new TicketReserve.TicketDetails("SectionA", 1, List.of(3)),
                new TicketReserve.TicketDetails("SectionB", 1, List.of(4))
        )));
        requestIds = response.reserveTickets.stream().map(t -> t.reserveId).collect(Collectors.toList());
        requestPrice = response.reserveTickets.stream().map(t -> t.price).reduce(0.0f, Float::sum);

        var reqIds = requestIds.stream().map(TicketPurchase.TicketDetails::new).collect(Collectors.toList());
        purchaseAPI.addCustomer("test_customer2", 100);
        var redirectUrl = controller.ticketPurchase(session, new TicketPurchase.Request(authToken2,
                "https://example.com/success", "https://example.com/cancel", reqIds)).redirectUrl;
        session = TestHelper.commitMakeSession(model, session);
        purchaseAPI.fulfillOrder(redirectUrl, "test_customer2");

        Set<String> ticketIds2 = controller.ticketBookings(session, Map.of("event_id", eventId, "auth_token", authToken2)).tickets;
        assertTrue(ticketIds2.size() == 2);
        assertTrue(ticketIds.size() == 2);

    }

    @Test
    public void testTicketBookingsExceptions () {
        assertThrows(ForbiddenException.class, () -> controller.ticketBookings(session, Map.of("event_id", UUID.randomUUID().toString(), "auth_token", authToken)));
        assertThrows(UnauthorizedException.class, () -> controller.ticketBookings(session, Map.of("event_id", eventId, "auth_token", "authToken")));
    }

    @Test 
    public void testTicketView () {
        Set<String> ticketIds = controller.ticketBookings(session, Map.of("event_id", eventId, "auth_token", authToken)).tickets;
        User user = controller.authenticateToken(session, authToken);
        int count = 0;
        for (String ticketId : ticketIds) {
            var response = controller.ticketView(session, Map.of("ticket_id", ticketId)); 
            assertEquals(eventId, response.eventId);
            assertEquals(user.getId().toString(), response.userId);
            if (count == 0) {
                assertEquals("SectionB", response.section);
                assertEquals(2, response.seatNum);
            } else {
                assertEquals("SectionA", response.section);
                assertEquals(1, response.seatNum);
            }
            count += 1;
        }
    }

    @Test 
    public void testTicketViewExceptions () {
        Set<String> ticketIds = controller.ticketBookings(session, Map.of("event_id", eventId, "auth_token", authToken)).tickets;
        for (String ticketId : ticketIds) {
            assertThrows(BadRequestException.class, () -> controller.ticketView(session, Map.of("ticketid", ticketId)));
            assertThrows(ForbiddenException.class, () -> controller.ticketView(session, Map.of("ticket_id", UUID.randomUUID().toString())));
            break;
        }
        
    }
}
