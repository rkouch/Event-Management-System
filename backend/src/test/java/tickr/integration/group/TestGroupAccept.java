package tickr.integration.group;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import spark.Spark;
import tickr.CreateEventReqBuilder;
import tickr.TestHelper;
import tickr.application.apis.ApiLocator;
import tickr.application.apis.email.IEmailAPI;
import tickr.application.apis.purchase.IPurchaseAPI;
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
import tickr.application.serialised.responses.AuthTokenResponse;
import tickr.application.serialised.responses.CreateEventResponse;
import tickr.application.serialised.responses.EventAttendeesResponse;
import tickr.application.serialised.responses.GroupAcceptResponse;
import tickr.application.serialised.responses.GroupCreateResponse;
import tickr.application.serialised.responses.GroupIdsResponse;
import tickr.application.serialised.responses.TicketBookingsResponse;
import tickr.application.serialised.responses.UserIdResponse;
import tickr.mock.MockEmailAPI;
import tickr.mock.MockHttpPurchaseAPI;
import tickr.persistence.DataModel;
import tickr.persistence.HibernateModel;
import tickr.server.Server;
import tickr.util.HTTPHelper;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
public class TestGroupAccept {
    private DataModel hibernateModel;
    private HTTPHelper httpHelper;

    private MockHttpPurchaseAPI purchaseAPI;
    private MockEmailAPI emailAPI;

    private String authToken;
    private String authToken2;
    private String authToken3;

    private String inviteId1;
    private String inviteId2;

    private String eventId;
    private String groupId;

    private String requestId;
    private List<String> reserveIdList;
    private float requestPrice;

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    @BeforeEach
    public void setup () {
        hibernateModel = new HibernateModel("hibernate-test.cfg.xml");
        purchaseAPI = new MockHttpPurchaseAPI("http://localhost:8080");
        ApiLocator.addLocator(IPurchaseAPI.class, () -> purchaseAPI);

        Server.start(8080, null, hibernateModel);
        httpHelper = new HTTPHelper("http://localhost:8080");
        Spark.awaitInitialization();

        var response = httpHelper.post("/api/user/register", new UserRegisterRequest("TestUsername", "Test", "User", "test@example.com",
                "Password123!", "2010-10-07"));
        assertEquals(200, response.getStatus());
        authToken = response.getBody(AuthTokenResponse.class).authToken;

        response = httpHelper.post("/api/user/register", new UserRegisterRequest("TestUsername", "Test", "User", "test2@example.com",
                "Password123!", "2010-10-07"));
        assertEquals(200, response.getStatus());
        authToken2 = response.getBody(AuthTokenResponse.class).authToken;

        response = httpHelper.post("/api/user/register", new UserRegisterRequest("TestUsername", "Test", "User", "test3@example.com",
                "Password123!", "2010-10-07"));
        assertEquals(200, response.getStatus());
        authToken3 = response.getBody(AuthTokenResponse.class).authToken;

        startTime = LocalDateTime.now(ZoneId.of("UTC")).plus(Duration.ofDays(1));
        endTime = startTime.plus(Duration.ofHours(1));

        List<CreateEventRequest.SeatingDetails> seatingDetails = List.of(
                new CreateEventRequest.SeatingDetails("test_section", 10, 1, true),
                new CreateEventRequest.SeatingDetails("test_section2", 20, 4, true)
        );

        response = httpHelper.post("/api/event/create", new CreateEventReqBuilder()
                .withStartDate(startTime.minusMinutes(2))
                .withEndDate(endTime)
                .withSeatingDetails(seatingDetails)
                .build(authToken));
        assertEquals(200, response.getStatus());
        eventId = response.getBody(CreateEventResponse.class).event_id;

        response = httpHelper.put("/api/event/edit", new EditEventRequest(eventId, authToken, null, null, null, null,
                null, null, null, null, null, null, true));
        assertEquals(200, response.getStatus());

        response = httpHelper.post("/api/ticket/reserve", new TicketReserve.Request(authToken, eventId, startTime, List.of(
                new TicketReserve.TicketDetails("test_section", 1, List.of(1)),
                new TicketReserve.TicketDetails("test_section2", 2, List.of(2, 3))
        )));
        assertEquals(200, response.getStatus());
        var reserveResponse = response.getBody(TicketReserve.Response.class);
        reserveIdList = reserveResponse.reserveTickets.stream()
                .map(ReserveDetails::getReserveId)
                .collect(Collectors.toList());

        response = httpHelper.post("/api/group/create", new GroupCreateRequest(authToken, reserveIdList, reserveIdList.get(2)));
        assertEquals(200, response.getStatus());
        groupId = response.getBody(GroupCreateResponse.class).groupId;
        
        emailAPI = new MockEmailAPI();
        ApiLocator.addLocator(IEmailAPI.class, () -> emailAPI);

        response = httpHelper.post("/api/group/invite", 
                new GroupInviteRequest(authToken, groupId, reserveIdList.get(0), "test2@example.com"));
        assertEquals(200, response.getStatus());
        response = httpHelper.post("/api/group/invite", 
                new GroupInviteRequest(authToken, groupId, reserveIdList.get(1), "test3@example.com"));
        assertEquals(200, response.getStatus());

        var message1 = emailAPI.getSentMessages().get(0);
        var message2 = emailAPI.getSentMessages().get(1);
        inviteId1 = message1.getBody().split("/group/")[1].split("\"")[0];
        inviteId2 = message2.getBody().split("/group/")[1].split("\"")[0];
    }

    @AfterEach
    public void cleanup () {
        Spark.stop();
        hibernateModel.cleanup();
        Spark.awaitStop();

        ApiLocator.clearLocator(IPurchaseAPI.class);
    }    

    @Test 
    public void testAcceptInvite() {
        var response = httpHelper.post("/api/group/accept", new GroupAcceptRequest(authToken2, inviteId1));
        assertEquals(200, response.getStatus());
        assertEquals(reserveIdList.get(0), response.getBody(GroupAcceptResponse.class).reserveId);

        response = httpHelper.post("/api/group/accept", new GroupAcceptRequest(authToken2, inviteId1));
        assertEquals(400, response.getStatus());


        response = httpHelper.post("/api/group/accept", new GroupAcceptRequest(authToken3, inviteId2));
        assertEquals(200, response.getStatus());
        assertEquals(reserveIdList.get(1), response.getBody(GroupAcceptResponse.class).reserveId);

        response = httpHelper.post("/api/group/accept", new GroupAcceptRequest(authToken2, inviteId2));
        assertEquals(400, response.getStatus());
    }

    @Test 
    public void testDenyInvite () {
        var response = httpHelper.post("/api/group/deny", new GroupDenyRequest(inviteId1));
        assertEquals(200, response.getStatus());
        response = httpHelper.post("/api/group/deny", new GroupDenyRequest(inviteId1));
        assertEquals(400, response.getStatus());

        response = httpHelper.post("/api/group/deny", new GroupDenyRequest(inviteId2));
        assertEquals(200, response.getStatus());
        response = httpHelper.post("/api/group/deny", new GroupDenyRequest(inviteId2));
        assertEquals(400, response.getStatus());
    }

    @Test 
    public void testExceptions() {
        var response = httpHelper.post("/api/group/accept", new GroupAcceptRequest(null, inviteId1));
        assertEquals(401, response.getStatus());
        response = httpHelper.post("/api/group/accept", new GroupAcceptRequest(authToken2, null));
        assertEquals(400, response.getStatus());
        response = httpHelper.post("/api/group/accept", new GroupAcceptRequest(authToken2, UUID.randomUUID().toString()));
        assertEquals(400, response.getStatus());

        response = httpHelper.post("/api/group/deny", new GroupDenyRequest(null));
        assertEquals(400, response.getStatus());
        response = httpHelper.post("/api/group/deny", new GroupDenyRequest(UUID.randomUUID().toString()));
        assertEquals(400, response.getStatus());
    }
}
