package tickr.unit.reviews;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import tickr.CreateEventReqBuilder;
import tickr.TestHelper;
import tickr.application.TickrController;
import tickr.application.apis.ApiLocator;
import tickr.application.apis.purchase.IPurchaseAPI;
import tickr.application.serialised.combined.ReviewCreate;
import tickr.application.serialised.combined.TicketPurchase;
import tickr.application.serialised.combined.TicketReserve;
import tickr.application.serialised.requests.CreateEventRequest;
import tickr.application.serialised.requests.UserRegisterRequest;
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
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class TestReviewCreateView {
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
                new CreateEventRequest.SeatingDetails("test_section", 10, 1, true),
                new CreateEventRequest.SeatingDetails("test_section2", 20, 4, true)
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

        var response = controller.ticketReserve(session, new TicketReserve.Request(authToken, eventId, startTime, List.of(
                new TicketReserve.TicketDetails("test_section", 1, List.of()),
                new TicketReserve.TicketDetails("test_section2", 1, List.of())
        )));
        session = TestHelper.commitMakeSession(model, session);
        var requestIds = response.reserveTickets.stream().map(t -> t.reserveId).collect(Collectors.toList());
        var purchaseAPI = new MockUnitPurchaseAPI(controller, model);
        ApiLocator.addLocator(IPurchaseAPI.class, () -> purchaseAPI);
        var redirectUrl = controller.ticketPurchase(session, new TicketPurchase.Request(authToken,
                "http://example.com/success", "http://example.com/failure", requestIds.stream()
                .map(i -> new TicketPurchase.TicketDetails(i, null, null, null)).collect(Collectors.toList()))).redirectUrl;
        session = TestHelper.commitMakeSession(model, session);
        purchaseAPI.addCustomer("test_customer", 10);
        assertEquals("http://example.com/success", purchaseAPI.fulfillOrder(redirectUrl, "test_customer"));
        //session = TestHelper.commitMakeSession(model, session);
    }

    @AfterEach
    public void cleanup () {
        model.cleanup();
        ApiLocator.clearLocator(IPurchaseAPI.class);
    }

    @Test
    public void testCreateBadRequest () {
        assertThrows(UnauthorizedException.class, () -> controller.reviewCreate(session,
                new ReviewCreate.Request(null, null, null, null, 1.0f)));
        assertThrows(BadRequestException.class, () -> controller.reviewCreate(session,
                new ReviewCreate.Request(authToken, null, null, null, 1.0f)));
        assertThrows(BadRequestException.class, () -> controller.reviewCreate(session,
                new ReviewCreate.Request(authToken, eventId, null, null, 1.0f)));
        assertThrows(BadRequestException.class, () -> controller.reviewCreate(session,
                new ReviewCreate.Request(authToken, eventId, "title", null, 1.0f)));
        assertThrows(BadRequestException.class, () -> controller.reviewCreate(session,
                new ReviewCreate.Request(authToken, eventId, null, "text", 1.0f)));
        assertThrows(ForbiddenException.class, () -> controller.reviewCreate(session,
                new ReviewCreate.Request(authToken, eventId, "title", "text", -1.0f)));
        assertThrows(ForbiddenException.class, () -> controller.reviewCreate(session,
                new ReviewCreate.Request(authToken, eventId, "title", "text", 11.0f)));
        assertThrows(BadRequestException.class, () -> controller.reviewCreate(session,
                new ReviewCreate.Request(authToken, eventId, "", "text", 1.0f)));

        assertThrows(UnauthorizedException.class, () -> controller.reviewCreate(session,
                new ReviewCreate.Request(TestHelper.makeFakeJWT(), eventId, "title", "text", 1.0f)));
        assertThrows(ForbiddenException.class, () -> controller.reviewCreate(session,
                new ReviewCreate.Request(authToken, UUID.randomUUID().toString(), "title", "text", 1.0f)));
    }

    @Test
    public void testViewBadRequest () {
        assertThrows(BadRequestException.class, () -> controller.reviewsView(session,
                Map.of()));
        assertThrows(BadRequestException.class, () -> controller.reviewsView(session,
                Map.of("event_id", eventId)));
        assertThrows(BadRequestException.class, () -> controller.reviewsView(session,
                Map.of("event_id", eventId, "page_start", "1")));
        assertThrows(BadRequestException.class, () -> controller.reviewsView(session,
                Map.of("event_id", eventId, "max_results", "1")));
        assertThrows(ForbiddenException.class, () -> controller.reviewsView(session,
                Map.of("event_id", UUID.randomUUID().toString(), "page_start", "0", "max_results", "10")));
        assertThrows(UnauthorizedException.class, () -> controller.reviewsView(session,
                Map.of("auth_token", TestHelper.makeFakeJWT(), "event_id", eventId, "page_start", "0", "max_results", "10")));

        assertThrows(ForbiddenException.class, () -> controller.reviewsView(session,
                Map.of("auth_token", authToken, "event_id", eventId, "page_start", "-1", "max_results", "10")));
        assertThrows(ForbiddenException.class, () -> controller.reviewsView(session,
                Map.of("auth_token", authToken, "event_id", eventId, "page_start", "0", "max_results", "0")));
        assertThrows(ForbiddenException.class, () -> controller.reviewsView(session,
                Map.of("auth_token", authToken, "event_id", eventId, "page_start", "0", "max_results", "-1")));
        assertThrows(ForbiddenException.class, () -> controller.reviewsView(session,
                Map.of("auth_token", authToken, "event_id", eventId, "page_start", "-1", "max_results", "257")));
    }

    @Test
    public void testReviewCreate () {
        var reviewId = controller.reviewCreate(session,
                new ReviewCreate.Request(authToken, eventId, "test", "testing", 1.0f)).reviewId;
        session = TestHelper.commitMakeSession(model, session);
        assertNotNull(reviewId);

        var viewResponse = controller.reviewsView(session,
                Map.of("event_id", eventId, "page_start", "0", "max_results", "10"));
        session = TestHelper.commitMakeSession(model, session);
        assertEquals(1, viewResponse.numResults);
        assertEquals(1, viewResponse.reviews.size());

        var review = viewResponse.reviews.get(0);
        assertEquals(reviewId, review.reviewId);
        assertEquals(controller.userSearch(session, Map.of("email", "test@example.com")).userId, review.authorId);
        assertEquals("test", review.title);
        assertEquals("testing", review.text);
        assertEquals(1.0f, review.rating);
    }

    @Test
    public void testReviewTwice () {
        var reviewId = controller.reviewCreate(session,
                new ReviewCreate.Request(authToken, eventId, "test", "testing", 1.0f)).reviewId;
        session = TestHelper.commitMakeSession(model, session);
        assertThrows(ForbiddenException.class, () -> controller.reviewCreate(session,
                new ReviewCreate.Request(authToken, eventId, "test", "testing", 1.0f)));
        session.rollback();
        session.close();
        session = model.makeSession();

        var viewResponse = controller.reviewsView(session,
                Map.of("event_id", eventId, "page_start", "0", "max_results", "10"));
        TestHelper.commitMakeSession(model, session);
        assertEquals(1, viewResponse.numResults);
        assertEquals(1, viewResponse.reviews.size());
    }
}
