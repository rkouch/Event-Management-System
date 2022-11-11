package tickr.unit.group;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.stripe.model.Event;

import static org.junit.jupiter.api.Assertions.*;
import tickr.CreateEventReqBuilder;
import tickr.TestHelper;
import tickr.application.TickrController;
import tickr.application.apis.ApiLocator;
import tickr.application.apis.email.IEmailAPI;
import tickr.application.apis.location.ILocationAPI;
import tickr.application.apis.purchase.IPurchaseAPI;
import tickr.application.entities.Invitation;
import tickr.application.entities.PurchaseItem;
import tickr.application.entities.SeatingPlan;
import tickr.application.entities.Ticket;
import tickr.application.entities.TicketReservation;
import tickr.application.serialised.combined.TicketPurchase;
import tickr.application.serialised.combined.TicketReserve;
import tickr.application.serialised.combined.TicketReserve.ReserveDetails;
import tickr.application.serialised.requests.CreateEventRequest;
import tickr.application.serialised.requests.EditEventRequest;
import tickr.application.serialised.requests.GroupAcceptRequest;
import tickr.application.serialised.requests.GroupCreateRequest;
import tickr.application.serialised.requests.GroupDenyRequest;
import tickr.application.serialised.requests.GroupInviteRequest;
import tickr.application.serialised.requests.UserRegisterRequest;
import tickr.application.serialised.responses.EventAttendeesResponse;
import tickr.mock.AbstractMockPurchaseAPI;
import tickr.mock.MockEmailAPI;
import tickr.mock.MockLocationApi;
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
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class TestGroupAccept {
    private DataModel model;
    private TickrController controller;
    private ModelSession session;
    private AbstractMockPurchaseAPI purchaseAPI;
    private MockEmailAPI emailAPI;

    private String eventId;
    private String authToken; 
    private String authToken2;
    private String authToken3;

    private String inviteId1;
    private String inviteId2;

    private ZonedDateTime startTime;
    private ZonedDateTime endTime;

    private List<String> requestIds;
    private float requestPrice;

    private List<String> reserveIdList;

    private String groupId;

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
        
        authToken2 = controller.userRegister(session,
        new UserRegisterRequest("test", "first", "last", "test2@example.com",
                "Password123!", "2022-04-14")).authToken;

        session = TestHelper.commitMakeSession(model, session);

        authToken3 = controller.userRegister(session,
        new UserRegisterRequest("test", "first", "last", "test3@example.com",
                "Password123!", "2022-04-14")).authToken;

        session = TestHelper.commitMakeSession(model, session);

        startTime = ZonedDateTime.now(ZoneId.of("UTC")).plus(Duration.ofDays(1));
        endTime = startTime.plus(Duration.ofHours(1));
        
        eventId = controller.createEvent(session, new CreateEventReqBuilder()
            .withEventName("Test Event")
            .withSeatingDetails(seatingDetails)
            .withStartDate(startTime.minusMinutes(2))
            .withEndDate(endTime)
            .build(authToken)).event_id;
        
        session = TestHelper.commitMakeSession(model, session);

        controller.editEvent(session, new EditEventRequest(eventId, authToken, null, null, null, null,
                null, null, null, null, null, null, true));
        session = TestHelper.commitMakeSession(model, session);

        var response = controller.ticketReserve(session, new TicketReserve.Request(authToken, eventId, startTime, List.of(
                new TicketReserve.TicketDetails("SectionA", 1, List.of(1)),
                new TicketReserve.TicketDetails("SectionB", 2, List.of(2, 3))
        )));
        session = TestHelper.commitMakeSession(model, session);
        reserveIdList = response.reserveTickets.stream()
                .map(ReserveDetails::getReserveId)
                .collect(Collectors.toList());
        
        groupId = controller.groupCreate(session, new GroupCreateRequest(authToken, reserveIdList, reserveIdList.get(2))).groupId;
        session = TestHelper.commitMakeSession(model, session);

        emailAPI = new MockEmailAPI();
        ApiLocator.addLocator(IEmailAPI.class, () -> emailAPI);

        controller.groupInvite(session, new GroupInviteRequest(authToken, groupId, reserveIdList.get(0), "test2@example.com"));
        session = TestHelper.commitMakeSession(model, session);
        controller.groupInvite(session, new GroupInviteRequest(authToken, groupId, reserveIdList.get(1), "test3@example.com"));
        session = TestHelper.commitMakeSession(model, session);

        var message1 = emailAPI.getSentMessages().get(0);
        var message2 = emailAPI.getSentMessages().get(1);
        inviteId1 = message1.getBody().split("/group/")[1].split("\"")[0];
        inviteId2 = message2.getBody().split("/group/")[1].split("\"")[0];
    }

    @AfterEach
    public void cleanup () {
        model.cleanup();
        ApiLocator.clearLocator(IEmailAPI.class);
        ApiLocator.clearLocator(ILocationAPI.class);
    }

    @Test 
    public void testAcceptInvite() {
        var invitations = session.getAll(Invitation.class);
        assertEquals(2, invitations.size());

        var response = controller.groupAccept(session, new GroupAcceptRequest(authToken2, inviteId1));
        session = TestHelper.commitMakeSession(model, session);

        assertEquals(reserveIdList.get(0), response.reserveId);
        invitations = session.getAll(Invitation.class);
        assertEquals(1, invitations.size());

        assertThrows(BadRequestException.class, () -> controller.groupAccept(session, new GroupAcceptRequest(authToken2, inviteId1)));

        response = controller.groupAccept(session, new GroupAcceptRequest(authToken2, inviteId2));
        session = TestHelper.commitMakeSession(model, session);

        assertEquals(reserveIdList.get(1), response.reserveId);
        invitations = session.getAll(Invitation.class);
        assertEquals(0, invitations.size());

        assertThrows(BadRequestException.class, () -> controller.groupAccept(session, new GroupAcceptRequest(authToken2, inviteId2)));
    }

    @Test 
    public void testDenyInvite() {
        assertDoesNotThrow(() -> controller.groupDeny(session, new GroupDenyRequest(inviteId1)));
        session = TestHelper.commitMakeSession(model, session);

        var invitations = session.getAll(Invitation.class);
        assertEquals(1, invitations.size());

        assertThrows(BadRequestException.class, () -> controller.groupDeny(session, new GroupDenyRequest(inviteId1)));

        assertDoesNotThrow(() -> controller.groupDeny(session, new GroupDenyRequest(inviteId2)));
        session = TestHelper.commitMakeSession(model, session);

        invitations = session.getAll(Invitation.class);
        assertEquals(0, invitations.size());

        assertThrows(BadRequestException.class, () -> controller.groupDeny(session, new GroupDenyRequest(inviteId2)));
    }

    @Test 
    public void testExceptions() {
        assertThrows(BadRequestException.class, () -> controller.groupAccept(session, new GroupAcceptRequest(authToken2, null)));
        assertThrows(BadRequestException.class, () -> controller.groupAccept(session, new GroupAcceptRequest(authToken2, UUID.randomUUID().toString())));
        assertThrows(UnauthorizedException.class, () -> controller.groupAccept(session, new GroupAcceptRequest(null, inviteId1)));
    
        assertThrows(BadRequestException.class, () -> controller.groupDeny(session, new GroupDenyRequest(null)));
        assertThrows(BadRequestException.class, () -> controller.groupDeny(session, new GroupDenyRequest(UUID.randomUUID().toString())));
    }
}
