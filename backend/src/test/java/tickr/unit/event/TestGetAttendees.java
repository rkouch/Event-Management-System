package tickr.unit.event;

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

public class TestGetAttendees {
    private DataModel model;
    private TickrController controller;
    private ModelSession session;
    private AbstractMockPurchaseAPI purchaseAPI;

    private String eventId;
    private String authToken; 
    private String authToken2;
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
        
        authToken2 = controller.userRegister(session,
        new UserRegisterRequest("test", "first", "last", "test2@example.com",
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

        var response2 = controller.ticketReserve(session, new TicketReserve.Request(authToken2, eventId, startTime, List.of(
                new TicketReserve.TicketDetails("SectionA", 2, List.of(3, 4)),
                new TicketReserve.TicketDetails("SectionB", 1, List.of(5))
        )));
        requestIds = response2.reserveTickets.stream().map(t -> t.reserveId).collect(Collectors.toList());
        requestPrice = response2.reserveTickets.stream().map(t -> t.price).reduce(0.0f, Float::sum);

        var reqIds2 = requestIds.stream().map(TicketPurchase.TicketDetails::new).collect(Collectors.toList());
        purchaseAPI.addCustomer("test_customer2", 100);
        var redirectUrl2 = controller.ticketPurchase(session, new TicketPurchase.Request(authToken2,
                "https://example.com/success", "https://example.com/cancel", reqIds2)).redirectUrl;
        session = TestHelper.commitMakeSession(model, session);
        purchaseAPI.fulfillOrder(redirectUrl2, "test_customer2");
    }

    @Test
    public void testGetAttendees () {
        var attendees = controller.GetEventAttendees(session, Map.of("auth_token", authToken, "event_id", eventId)).getAttendees();
        // assertEquals(2, attendees.size());
        assertTrue(attendees.get(0).getTickets().size() == 2 || attendees.get(0).getTickets().size() == 3);
        // assertTrue(attendees.get(1).getTickets().size() == 2 || attendees.get(1).getTickets().size() == 3);
    }
}
