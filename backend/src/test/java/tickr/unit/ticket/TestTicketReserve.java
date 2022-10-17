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
import tickr.application.serialised.combined.TicketReserve;
import tickr.application.serialised.requests.CreateEventRequest;
import tickr.application.serialised.requests.UserRegisterRequest;
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

public class TestTicketReserve {
    private DataModel model;
    private TickrController controller;

    private ModelSession session;
    private String authToken;
    private String eventId;

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    private List<CreateEventRequest.SeatingDetails> seatingDetails;

    @BeforeEach
    public void setup () {
        model = new HibernateModel("hibernate-test.cfg.xml");
        controller = new TickrController();

        startTime = LocalDateTime.now(ZoneId.of("UTC")).plus(Duration.ofDays(1));
        endTime = startTime.plus(Duration.ofHours(1));

        seatingDetails = List.of(
                new CreateEventRequest.SeatingDetails("test_section", 10, 1),
                new CreateEventRequest.SeatingDetails("test_section2", 20, 4)
        );

        session = model.makeSession();
        authToken = controller.userRegister(session, new UserRegisterRequest("TestUsername", "Test", "User", "test@example.com",
                "Password123!", "2010-10-07")).authToken;
        session = TestHelper.commitMakeSession(model, session);

        eventId = controller.createEvent(session, new CreateEventReqBuilder()
                .withStartDate(startTime)
                .withEndDate(endTime)
                .withSeatingDetails(seatingDetails)
                .build(authToken)).event_id;
        session = TestHelper.commitMakeSession(model, session);
    }

    @AfterEach
    public void cleanup () {
        model.cleanup();
        ApiLocator.clearLocator(IPurchaseAPI.class);
    }

    @Test
    public void testBadRequest () {
        var ticketDetails = List.of(new TicketReserve.TicketDetails("test_section"));

        assertThrows(UnauthorizedException.class, () -> controller.ticketReserve(session, new TicketReserve.Request()));
        assertThrows(UnauthorizedException.class, () -> controller.ticketReserve(session,
                new TicketReserve.Request(TestHelper.makeFakeJWT(), eventId, startTime, ticketDetails)));

        assertThrows(BadRequestException.class, () -> controller.ticketReserve(session,
                new TicketReserve.Request(authToken, null, startTime, ticketDetails)));
        assertThrows(BadRequestException.class, () -> controller.ticketReserve(session,
                new TicketReserve.Request(authToken, eventId, (String)null, ticketDetails)));
        assertThrows(BadRequestException.class, () -> controller.ticketReserve(session,
                new TicketReserve.Request(authToken, eventId, startTime, List.of())));
        assertThrows(BadRequestException.class, () -> controller.ticketReserve(session,
                new TicketReserve.Request(authToken, eventId, startTime,
                        List.of(new TicketReserve.TicketDetails(null, "last", "test@example.com", "test_section")))));
        assertThrows(BadRequestException.class, () -> controller.ticketReserve(session,
                new TicketReserve.Request(authToken, eventId, startTime,
                        List.of(new TicketReserve.TicketDetails("first", null, "test@example.com", "test_section")))));
        assertThrows(BadRequestException.class, () -> controller.ticketReserve(session,
                new TicketReserve.Request(authToken, eventId, startTime,
                        List.of(new TicketReserve.TicketDetails("first", "last", "test@", "test_section")))));

        assertThrows(ForbiddenException.class, () -> controller.ticketReserve(session,
                new TicketReserve.Request(authToken, UUID.randomUUID().toString(), startTime, ticketDetails)));
        assertThrows(ForbiddenException.class, () -> controller.ticketReserve(session,
                new TicketReserve.Request(authToken, eventId, LocalDateTime.now(), ticketDetails)));
        assertThrows(ForbiddenException.class, () -> controller.ticketReserve(session,
                new TicketReserve.Request(authToken, eventId, LocalDateTime.now().plus(Duration.ofDays(365)), ticketDetails)));

        assertThrows(ForbiddenException.class, () -> controller.ticketReserve(session,
                new TicketReserve.Request(authToken, eventId, startTime, List.of(new TicketReserve.TicketDetails("testing")))));
        assertThrows(ForbiddenException.class, () -> controller.ticketReserve(session,
                new TicketReserve.Request(authToken, eventId, startTime,
                        List.of(new TicketReserve.TicketDetails("first", "last", "test@example.com", "test_section", 0)))));
        assertThrows(ForbiddenException.class, () -> controller.ticketReserve(session,
                new TicketReserve.Request(authToken, eventId, startTime,
                        List.of(new TicketReserve.TicketDetails("first", "last", "test@example.com", "test_section", -1)))));
        assertThrows(ForbiddenException.class, () -> controller.ticketReserve(session,
                new TicketReserve.Request(authToken, eventId, startTime,
                        List.of(new TicketReserve.TicketDetails("first", "last", "test@example.com", "test_section", 1000000)))));
    }

    @Test
    public void testOneTicket () {
        var ticketDetails = List.of(new TicketReserve.TicketDetails("test_section"));

        var response = controller.ticketReserve(session, new TicketReserve.Request(authToken, eventId, startTime, ticketDetails));

        assertNotNull(response.reserveId);
        assertEquals(1, Float.parseFloat(response.price));
    }

    @Test
    public void testMultipleTickets () {
        var ticketDetails = List.of(
                new TicketReserve.TicketDetails( null, null, null, "test_section", 4),
                new TicketReserve.TicketDetails( null, null, null, "test_section", 5),
                new TicketReserve.TicketDetails( null, null, null, "test_section", 6),
                new TicketReserve.TicketDetails( null, null, null, "test_section", 7)
        );

        var response = controller.ticketReserve(session, new TicketReserve.Request(authToken, eventId, startTime, ticketDetails));

        assertNotNull(response.reserveId);
        assertEquals(4, Float.parseFloat(response.price));
    }

    @Test
    public void testMultipleSections () {
        var ticketDetails = List.of(
                new TicketReserve.TicketDetails( null, null, null, "test_section", 4),
                new TicketReserve.TicketDetails(null, null, null, "test_section2", 5)
        );

        var response = controller.ticketReserve(session, new TicketReserve.Request(authToken, eventId, startTime, ticketDetails));

        assertNotNull(response.reserveId);
        assertEquals(5, Float.parseFloat(response.price));
    }
}
