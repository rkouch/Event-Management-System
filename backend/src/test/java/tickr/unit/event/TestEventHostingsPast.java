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
import tickr.application.apis.location.ILocationAPI;
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
import tickr.mock.MockLocationApi;
import tickr.mock.MockUnitPurchaseAPI;
import tickr.persistence.DataModel;
import tickr.persistence.HibernateModel;
import tickr.persistence.ModelSession;
import tickr.server.exceptions.BadRequestException;
import tickr.server.exceptions.ForbiddenException;
import tickr.server.exceptions.UnauthorizedException;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
public class TestEventHostingsPast {
    private DataModel model;
    private TickrController controller;
    private ModelSession session;
    private AbstractMockPurchaseAPI purchaseAPI;

    private String eventId;
    private String eventId2;
    private String eventId3;
    private String authToken; 
    private ZonedDateTime startTime;
    private ZonedDateTime endTime;

    @BeforeEach
    public void setup () {
        model = new HibernateModel("hibernate-test.cfg.xml");
        controller = new TickrController();

        purchaseAPI = new MockUnitPurchaseAPI(controller, model);
        ApiLocator.addLocator(IPurchaseAPI.class, () -> purchaseAPI);
        ApiLocator.addLocator(ILocationAPI.class, () -> new MockLocationApi(model));

        List<CreateEventRequest.SeatingDetails> seatingDetails = new ArrayList<>();
        seatingDetails.add(new CreateEventRequest.SeatingDetails("SectionA", 10, 50, true));
        seatingDetails.add(new CreateEventRequest.SeatingDetails("SectionB", 20, 30, true));

        session = model.makeSession();

        authToken = controller.userRegister(session,
        new UserRegisterRequest("test", "first", "last", "test1@example.com",
                "Password123!", "2022-04-14")).authToken;

        session = TestHelper.commitMakeSession(model, session);

        startTime = ZonedDateTime.now(ZoneId.of("UTC")).minus(Duration.ofDays(1));
        var startTime2 = ZonedDateTime.now(ZoneId.of("UTC")).minus(Duration.ofDays(2));
        var startTime3 = ZonedDateTime.now(ZoneId.of("UTC")).minus(Duration.ofDays(3));
        endTime = startTime.plus(Duration.ofHours(1));
        var endTime2 = startTime2.plus(Duration.ofHours(1));
        var endTime3 = startTime3.plus(Duration.ofHours(1));
        
        eventId = controller.createEventUnsafe(session, new CreateEventReqBuilder()
                .withEventName("Test Event1")
                .withSeatingDetails(seatingDetails)
                .withStartDate(startTime.minusMinutes(2))
                .withEndDate(endTime)
                .build(authToken)).event_id;

        controller.editEvent(session, new EditEventRequest(eventId, authToken, null, null, null, null, null, null, null, null, null, null, true, null));

        session = TestHelper.commitMakeSession(model, session);
        
        eventId2 = controller.createEventUnsafe(session, new CreateEventReqBuilder()
                .withEventName("Test Event2")
                .withSeatingDetails(seatingDetails)
                .withStartDate(startTime2.minusMinutes(2))
                .withEndDate(endTime2)
                .build(authToken)).event_id;
        
        controller.editEvent(session, new EditEventRequest(eventId2, authToken, null, null, null, null, null, null, null, null, null, null, true, null));

        session = TestHelper.commitMakeSession(model, session);
        
        eventId3 = controller.createEventUnsafe(session, new CreateEventReqBuilder()
                .withEventName("Test Event3")
                .withSeatingDetails(seatingDetails)
                .withStartDate(startTime3.minusMinutes(2))
                .withEndDate(endTime3)
                .build(authToken)).event_id;

        
        
        session = TestHelper.commitMakeSession(model, session);

        controller.editEvent(session, new EditEventRequest(eventId3, authToken, null, null, null, null, null, null, null, null, null, null, true, null));
    }

    @AfterEach
    public void cleanup () {
        model.cleanup();
        ApiLocator.clearLocator(ILocationAPI.class);
    }

    @Test 
    public void testEventHostingsPast () {
        int pageStart = 0;
        int maxResults = 10;
        var bookings = controller.eventHostingsPast(session, Map.of(
            "auth_token", authToken, 
            "page_start", Integer.toString(pageStart), 
            "max_results", Integer.toString(maxResults)
        ));
        assertEquals(3, bookings.eventIds.size());
        assertEquals(bookings.eventIds.get(0), eventId3);
        assertEquals(bookings.eventIds.get(1), eventId2);
        assertEquals(bookings.eventIds.get(2), eventId);
        assertEquals(3, bookings.numResults);
    }

    @Test 
    public void testPastCustomerBookingsAfter () {
        int pageStart = 0;
        int maxResults = 10;
        var bookings = controller.eventHostingsPast(session, Map.of(
            "auth_token", authToken, 
            "page_start", Integer.toString(pageStart), 
            "max_results", Integer.toString(maxResults),
            "after", ZonedDateTime.now(ZoneId.of("UTC")).minus(Duration.ofDays(3)).toString()
        ));
        assertEquals(2, bookings.eventIds.size());
        assertEquals(bookings.eventIds.get(0), eventId2);
        assertEquals(bookings.eventIds.get(1), eventId);
        assertEquals(2, bookings.numResults);
    }

    @Test
    public void testExceptions() {
        int numEvents = 16; 
        int pageStart = 0;
        assertThrows(BadRequestException.class, () ->
            controller.eventHostingsPast(session, Map.of("page_start", Integer.toString(pageStart), "max_results", Integer.toString(numEvents))));
        assertThrows(BadRequestException.class, () ->
            controller.eventHostingsPast(session, Map.of("auth_token", authToken, "max_results", Integer.toString(numEvents))));
        assertThrows(BadRequestException.class, () ->
            controller.eventHostingsPast(session, Map.of("auth_token", authToken, "page_start", Integer.toString(pageStart))));
        assertThrows(BadRequestException.class, () ->
            controller.eventHostingsPast(session, Map.of("auth_token", authToken, "page_start", Integer.toString(-1), "max_results", Integer.toString(numEvents))));
        assertThrows(BadRequestException.class, () ->
            controller.eventHostingsPast(session, Map.of("auth_token", authToken, "page_start", Integer.toString(pageStart), "max_results", Integer.toString(-1))));
        assertThrows(UnauthorizedException.class, () ->
            controller.eventHostingsPast(session, Map.of("auth_token", "authToken", "page_start", Integer.toString(pageStart), "max_results", Integer.toString(numEvents))));
        assertThrows(BadRequestException.class, () ->
            controller.eventHostingsPast(session, Map.of("auth_token", authToken, "page_start", Integer.toString(pageStart), "max_results", Integer.toString(numEvents), "after", " ")));
    }
}
