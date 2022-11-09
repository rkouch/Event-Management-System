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
import tickr.application.serialised.requests.GroupCreateRequest;
import tickr.application.serialised.requests.GroupInviteRequest;
import tickr.application.serialised.requests.UserRegisterRequest;
import tickr.application.serialised.responses.EventAttendeesResponse;
import tickr.mock.AbstractMockPurchaseAPI;
import tickr.mock.MockEmailAPI;
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
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.swing.GroupLayout.Group;
public class TestGroupInvitation {
    private DataModel model;
    private TickrController controller;
    private ModelSession session;
    private AbstractMockPurchaseAPI purchaseAPI;
    private MockEmailAPI emailAPI;
    
    private String eventId;
    private String authToken; 
    private String authToken2;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

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

        startTime = LocalDateTime.now(ZoneId.of("UTC")).plus(Duration.ofDays(1));
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
                new TicketReserve.TicketDetails("SectionB", 1, List.of(2))
        )));
        session = TestHelper.commitMakeSession(model, session);
        reserveIdList = response.reserveTickets.stream()
                .map(ReserveDetails::getReserveId)
                .collect(Collectors.toList());
        
        groupId = controller.groupCreate(session, new GroupCreateRequest(authToken, reserveIdList)).groupId;
        session = TestHelper.commitMakeSession(model, session);

        emailAPI = new MockEmailAPI();
        ApiLocator.addLocator(IEmailAPI.class, () -> emailAPI);
    }

    @AfterEach
    public void cleanup () {
        model.cleanup();
        ApiLocator.clearLocator(IEmailAPI.class);
    }

    @Test 
    public void testGroupInvite() {
        controller.groupInvite(session, new GroupInviteRequest(authToken, groupId, reserveIdList.get(0), "test1@example.com"));

        session = TestHelper.commitMakeSession(model, session);

        assertEquals(1, emailAPI.getSentMessages().size());
        var message = emailAPI.getSentMessages().get(0);
        assertEquals("test1@example.com", message.getToEmail());
        assertEquals("User group invitation", message.getSubject());
        var pattern = Pattern.compile("<a href=\"http://localhost:3000/ticket/purchase/group/(.*)\">.*</a>");
        var matcher = pattern.matcher(message.getBody());
        assertTrue(matcher.find());

        controller.groupInvite(session, new GroupInviteRequest(authToken, groupId, reserveIdList.get(0), "test1@example.com"));
        session = TestHelper.commitMakeSession(model, session);
        assertEquals(2, emailAPI.getSentMessages().size());

        var invitations = session.getAll(Invitation.class);
        assertEquals(2, invitations.size());
    }

    @Test 
    public void testExceptions () {
        assertThrows(BadRequestException.class, () -> controller.groupInvite(session, 
                new GroupInviteRequest(authToken, null, reserveIdList.get(0), "test1@example.com")));
        assertThrows(BadRequestException.class, () -> controller.groupInvite(session, 
                new GroupInviteRequest(authToken, UUID.randomUUID().toString(), reserveIdList.get(0), "test1@example.com")));
        assertThrows(BadRequestException.class, () -> controller.groupInvite(session, 
                new GroupInviteRequest(authToken, groupId, UUID.randomUUID().toString(), "test1@example.com")));
        assertThrows(BadRequestException.class, () -> controller.groupInvite(session, 
                new GroupInviteRequest(authToken, groupId, null, "test1@example.com")));
        assertThrows(BadRequestException.class, () -> controller.groupInvite(session, 
                new GroupInviteRequest(authToken, groupId, reserveIdList.get(0), null)));
        assertThrows(BadRequestException.class, () -> controller.groupInvite(session, 
                new GroupInviteRequest(authToken, groupId, reserveIdList.get(0), "email")));
        assertThrows(UnauthorizedException.class, () -> controller.groupInvite(session, 
                new GroupInviteRequest(null, groupId, reserveIdList.get(0), "test1@example.com")));
    }
}
