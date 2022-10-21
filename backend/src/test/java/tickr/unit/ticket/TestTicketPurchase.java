package tickr.unit.ticket;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import tickr.CreateEventReqBuilder;
import tickr.TestHelper;
import tickr.application.TickrController;
import tickr.application.apis.ApiLocator;
import tickr.application.apis.purchase.IPurchaseAPI;
import tickr.application.entities.EventReservation;
import tickr.application.entities.Ticket;
import tickr.application.entities.TicketReservation;
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

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class TestTicketPurchase {
    private DataModel model;
    private TickrController controller;
    private AbstractMockPurchaseAPI purchaseAPI;

    private ModelSession session;
    private String authToken;
    private String eventId;

    private String requestId;
    private List<String> requestIds;
    private float requestPrice;

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    @BeforeEach
    public void setup () {
        model = new HibernateModel("hibernate-test.cfg.xml");
        controller = new TickrController();

        purchaseAPI = new MockUnitPurchaseAPI(controller, model);
        ApiLocator.addLocator(IPurchaseAPI.class, () -> purchaseAPI);

        startTime = LocalDateTime.now(ZoneId.of("UTC")).plus(Duration.ofDays(1));
        endTime = startTime.plus(Duration.ofHours(1));

        List<CreateEventRequest.SeatingDetails> seatingDetails = List.of(
                new CreateEventRequest.SeatingDetails("test_section", 10, 1),
                new CreateEventRequest.SeatingDetails("test_section2", 20, 4)
        );

        session = model.makeSession();
        authToken = controller.userRegister(session, new UserRegisterRequest("TestUsername", "Test", "User", "test@example.com",
                "Password123!", "2010-10-07")).authToken;
        session = TestHelper.commitMakeSession(model, session);

        eventId = controller.createEvent(session, new CreateEventReqBuilder()
                .withStartDate(startTime.minusMinutes(2))
                .withEndDate(endTime)
                .withSeatingDetails(seatingDetails)
                .build(authToken)).event_id;
        session = TestHelper.commitMakeSession(model, session);

        var response = controller.ticketReserve(session, new TicketReserve.RequestNew(authToken, eventId, startTime, List.of(
                new TicketReserve.TicketDetailsNew("test_section", 1, List.of()),
                new TicketReserve.TicketDetailsNew("test_section2", 1, List.of())
        )));
        requestIds = response.reserveTickets.stream().map(t -> t.reserveId).collect(Collectors.toList());
        requestPrice = response.reserveTickets.stream().map(t -> t.price).reduce(0.0f, Float::sum);
    }

    @AfterEach
    public void cleanup () {
        model.cleanup();
        ApiLocator.clearLocator(IPurchaseAPI.class);
    }

    @Test
    public void testBadRequest () {
        var reqIds = requestIds.stream().map(TicketPurchase.TicketDetails::new).collect(Collectors.toList());
        assertThrows(BadRequestException.class, () -> controller.ticketPurchase(session, new TicketPurchase.RequestNew(authToken, null, null, null)));
        assertThrows(BadRequestException.class, () -> controller.ticketPurchase(session, new TicketPurchase.RequestNew(authToken,"testing", null, reqIds)));
        assertThrows(BadRequestException.class, () -> controller.ticketPurchase(session, new TicketPurchase.RequestNew(authToken, "testing", "testing", List.of())));
        assertThrows(BadRequestException.class, () -> controller.ticketPurchase(session, new TicketPurchase.RequestNew(authToken, null, "testing", reqIds)));

        assertThrows(UnauthorizedException.class, () -> controller.ticketPurchase(session, new TicketPurchase.RequestNew(null, "testing", "testing", reqIds)));
        assertThrows(UnauthorizedException.class, () -> controller.ticketPurchase(session, new TicketPurchase.RequestNew(TestHelper.makeFakeJWT(), "testing", "testing", reqIds)));

        assertThrows(ForbiddenException.class, () -> controller.ticketPurchase(session, new TicketPurchase.RequestNew(authToken,"http://testing.com", "http://testing.com",
                List.of(new TicketPurchase.TicketDetails(UUID.randomUUID().toString())))));

        var newUser = controller.userRegister(session, TestHelper.makeRegisterRequest()).authToken;
        session = TestHelper.commitMakeSession(model, session);
        assertThrows(ForbiddenException.class, () -> controller.ticketPurchase(session, new TicketPurchase.RequestNew(newUser,"http://testing.com", "http://testing.com", reqIds)));
    }

    @Test
    public void testPurchase () {
        var reqIds = requestIds.stream().map(TicketPurchase.TicketDetails::new).collect(Collectors.toList());
        purchaseAPI.addCustomer("test_customer", 10);
        var redirectUrl = controller.ticketPurchase(session, new TicketPurchase.RequestNew(authToken,
                "https://example.com/success", "https://example.com/cancel", reqIds)).redirectUrl;
        session = TestHelper.commitMakeSession(model, session);

        assertTrue(purchaseAPI.isUrlValid(redirectUrl));

        var result = purchaseAPI.fulfillOrder(redirectUrl, "test_customer");
        assertEquals("https://example.com/success", result);

        assertEquals(5, purchaseAPI.getCustomer("test_customer").getBalance());

        assertEquals(2, session.getAll(Ticket.class).size());
        assertEquals(0, session.getAll(TicketReservation.class).size());
        assertEquals(0, session.getAll(EventReservation.class).size());
    }

    @Test
    public void testCancel () {
        var reqIds = requestIds.stream().map(TicketPurchase.TicketDetails::new).collect(Collectors.toList());
        purchaseAPI.addCustomer("test_customer", 10);
        var redirectUrl = controller.ticketPurchase(session, new TicketPurchase.RequestNew(authToken,
                "https://example.com/success", "https://example.com/cancel", reqIds)).redirectUrl;
        session = TestHelper.commitMakeSession(model, session);

        var result = purchaseAPI.cancelOrder(redirectUrl);
        assertEquals("https://example.com/cancel", result);

        assertEquals(10, purchaseAPI.getCustomer("test_customer").getBalance());
        assertEquals(0, session.getAll(Ticket.class).size());
        assertEquals(0, session.getAll(TicketReservation.class).size());
        assertEquals(0, session.getAll(EventReservation.class).size());
    }

    @Test
    public void testLowBalance () {
        var reqIds = requestIds.stream().map(TicketPurchase.TicketDetails::new).collect(Collectors.toList());
        purchaseAPI.addCustomer("test_customer", 0.5f);
        var redirectUrl = controller.ticketPurchase(session, new TicketPurchase.RequestNew(authToken,
                "https://example.com/success", "https://example.com/cancel", reqIds)).redirectUrl;
        session = TestHelper.commitMakeSession(model, session);

        var result = purchaseAPI.fulfillOrder(redirectUrl, "test_customer");
        assertEquals("https://example.com/cancel", result);

        assertEquals(0.5f, purchaseAPI.getCustomer("test_customer").getBalance());
        assertEquals(0, session.getAll(Ticket.class).size());
        assertEquals(0, session.getAll(TicketReservation.class).size());
        assertEquals(0, session.getAll(EventReservation.class).size());
    }

    @Test
    public void testBadUrls () {
        var reqIds = requestIds.stream().map(TicketPurchase.TicketDetails::new).collect(Collectors.toList());
        assertThrows(BadRequestException.class, () -> controller.ticketPurchase(session, new TicketPurchase.RequestNew(authToken,
                "example.com/success", "https://example.com/cancel", reqIds)));
        session.rollback();
        session.close();
        session = model.makeSession();
        assertThrows(BadRequestException.class, () -> controller.ticketPurchase(session, new TicketPurchase.RequestNew(authToken,
                "https://example.com/success", "not-real://example.com/cancel", reqIds)));
    }
}
