package tickr.integration.group;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import spark.Spark;
import tickr.CreateEventReqBuilder;
import tickr.TestHelper;
import tickr.application.apis.ApiLocator;
import tickr.application.apis.email.IEmailAPI;
import tickr.application.apis.location.ILocationAPI;
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
import tickr.application.serialised.requests.GroupRemoveInviteRequest;
import tickr.application.serialised.requests.GroupRemoveMemberRequest;
import tickr.application.serialised.requests.UserRegisterRequest;
import tickr.application.serialised.responses.AuthTokenResponse;
import tickr.application.serialised.responses.CreateEventResponse;
import tickr.application.serialised.responses.EventAttendeesResponse;
import tickr.application.serialised.responses.GroupCreateResponse;
import tickr.application.serialised.responses.GroupDetailsResponse;
import tickr.application.serialised.responses.GroupIdsResponse;
import tickr.application.serialised.responses.TicketBookingsResponse;
import tickr.application.serialised.responses.TicketViewResponse;
import tickr.application.serialised.responses.UserIdResponse;
import tickr.mock.MockEmailAPI;
import tickr.mock.MockHttpPurchaseAPI;
import tickr.mock.MockLocationApi;
import tickr.persistence.DataModel;
import tickr.persistence.HibernateModel;
import tickr.server.Server;
import tickr.util.HTTPHelper;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestGroupRemoveInvite {
    private DataModel hibernateModel;
    private HTTPHelper httpHelper;

    private MockHttpPurchaseAPI purchaseAPI;
    private MockEmailAPI emailAPI;

    private String hostId;
    private String authToken;
    private String authToken2;
    private String authToken3;
    private String authToken4;
    private String authToken5;

    private String inviteId1;
    private String inviteId2;
    private String inviteId3;
    private String inviteId4;

    private String eventId;

    private String requestId;
    private List<String> reserveIdList;
    private float requestPrice;

    private ZonedDateTime startTime;
    private ZonedDateTime endTime;

    private String groupId;

    @BeforeEach
    public void setup () {
        hibernateModel = new HibernateModel("hibernate-test.cfg.xml");
        purchaseAPI = new MockHttpPurchaseAPI("http://localhost:8080");
        ApiLocator.addLocator(IPurchaseAPI.class, () -> purchaseAPI);
        ApiLocator.addLocator(ILocationAPI.class, () -> new MockLocationApi(hibernateModel));

        Server.start(8080, null, hibernateModel);
        httpHelper = new HTTPHelper("http://localhost:8080");
        Spark.awaitInitialization();

        var response = httpHelper.post("/api/user/register", new UserRegisterRequest("TestUsername", "Test", "User", "test1@example.com",
                "Password123!", "2010-10-07"));
        assertEquals(200, response.getStatus());
        authToken = response.getBody(AuthTokenResponse.class).authToken;

        response =  httpHelper.get("/api/user/search", Map.of("email", "test1@example.com"));
        assertEquals(200, response.getStatus());
        hostId = response.getBody(UserIdResponse.class).userId;

        response = httpHelper.post("/api/user/register", new UserRegisterRequest("TestUsername", "Test", "User", "test2@example.com",
                "Password123!", "2010-10-07"));
        assertEquals(200, response.getStatus());
        authToken2 = response.getBody(AuthTokenResponse.class).authToken;

        response = httpHelper.post("/api/user/register", new UserRegisterRequest("TestUsername", "Test", "User", "test3@example.com",
                "Password123!", "2010-10-07"));
        assertEquals(200, response.getStatus());
        authToken3 = response.getBody(AuthTokenResponse.class).authToken;

        response = httpHelper.post("/api/user/register", new UserRegisterRequest("TestUsername", "Test", "User", "test4@example.com",
                "Password123!", "2010-10-07"));
        assertEquals(200, response.getStatus());
        authToken4 = response.getBody(AuthTokenResponse.class).authToken;

        response = httpHelper.post("/api/user/register", new UserRegisterRequest("TestUsername", "Test", "User", "test5@example.com",
                "Password123!", "2010-10-07"));
        assertEquals(200, response.getStatus());
        authToken5 = response.getBody(AuthTokenResponse.class).authToken;

        startTime = ZonedDateTime.now(ZoneId.of("UTC")).plus(Duration.ofDays(1));
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
                null, null, null, null, null, null, true, null));
        assertEquals(200, response.getStatus());

        response = httpHelper.post("/api/ticket/reserve", new TicketReserve.Request(authToken, eventId, startTime, List.of(
                new TicketReserve.TicketDetails("test_section", 3, List.of(1, 2, 3)),
                new TicketReserve.TicketDetails("test_section2", 2, List.of(1, 2))
        )));
        assertEquals(200, response.getStatus());
        var reserveResponse = response.getBody(TicketReserve.Response.class);
        reserveIdList = reserveResponse.reserveTickets.stream()
                .map(ReserveDetails::getReserveId)
                .collect(Collectors.toList());

        response = httpHelper.post("/api/group/create", new GroupCreateRequest(authToken, reserveIdList, reserveIdList.get(0)));
        assertEquals(200, response.getStatus());
        groupId = response.getBody(GroupCreateResponse.class).groupId;
        
        emailAPI = new MockEmailAPI();
        ApiLocator.addLocator(IEmailAPI.class, () -> emailAPI);

        response = httpHelper.post("/api/group/invite", new GroupInviteRequest(authToken, groupId, reserveIdList.get(1), "test2@example.com"));
        assertEquals(200, response.getStatus());
        response = httpHelper.post("/api/group/invite", new GroupInviteRequest(authToken, groupId, reserveIdList.get(2), "test3@example.com"));
        assertEquals(200, response.getStatus());
        response = httpHelper.post("/api/group/invite", new GroupInviteRequest(authToken, groupId, reserveIdList.get(3), "test4@example.com"));
        assertEquals(200, response.getStatus());
        response = httpHelper.post("/api/group/invite", new GroupInviteRequest(authToken, groupId, reserveIdList.get(4), "test5@example.com"));
        assertEquals(200, response.getStatus());

        inviteId1 = emailAPI.getSentMessages().get(0).getBody().split("/group/")[1].split("\"")[0];
        inviteId2 = emailAPI.getSentMessages().get(1).getBody().split("/group/")[1].split("\"")[0];
        inviteId3 = emailAPI.getSentMessages().get(2).getBody().split("/group/")[1].split("\"")[0];
        inviteId4 = emailAPI.getSentMessages().get(3).getBody().split("/group/")[1].split("\"")[0];
    }


    @AfterEach
    public void cleanup () {
        Spark.stop();
        hibernateModel.cleanup();
        Spark.awaitStop();

        ApiLocator.clearLocator(IPurchaseAPI.class);
        ApiLocator.clearLocator(ILocationAPI.class);
    }

    @Test 
    public void testGroupRemoveInvite () {
        var response = httpHelper.get("/api/group/details", Map.of("auth_token", authToken, "group_id", groupId));
        assertEquals(200, response.getStatus());
        assertEquals(4, response.getBody(GroupDetailsResponse.class).pendingInvites.size());
        
        response = httpHelper.delete("/api/group/invite/remove", new GroupRemoveInviteRequest(authToken, inviteId1, groupId));
        assertEquals(200, response.getStatus());
        response = httpHelper.delete("/api/group/invite/remove", new GroupRemoveInviteRequest(authToken, inviteId2, groupId));
        assertEquals(200, response.getStatus());

        response = httpHelper.get("/api/group/details", Map.of("auth_token", authToken, "group_id", groupId));
        assertEquals(200, response.getStatus());

        assertEquals(2, response.getBody(GroupDetailsResponse.class).pendingInvites.size());
    }

    @Test 
    public void testExceptions() {
        var response = httpHelper.delete("/api/group/invite/remove", new GroupRemoveInviteRequest(null, inviteId1, groupId));
        assertEquals(400, response.getStatus());
        response = httpHelper.delete("/api/group/invite/remove", new GroupRemoveInviteRequest(authToken, null, groupId));
        assertEquals(400, response.getStatus());
        response = httpHelper.delete("/api/group/invite/remove", new GroupRemoveInviteRequest(authToken, inviteId1, null));
        assertEquals(400, response.getStatus());
        response = httpHelper.delete("/api/group/invite/remove", new GroupRemoveInviteRequest(TestHelper.makeFakeJWT(), inviteId1, groupId));
        assertEquals(401, response.getStatus());
        response = httpHelper.delete("/api/group/invite/remove", new GroupRemoveInviteRequest(authToken, inviteId1, UUID.randomUUID().toString()));
        assertEquals(403, response.getStatus());
        response = httpHelper.delete("/api/group/invite/remove", new GroupRemoveInviteRequest(authToken2, inviteId1, groupId));
        assertEquals(400, response.getStatus());

        response = httpHelper.delete("/api/group/invite/remove", new GroupRemoveInviteRequest(authToken, inviteId1, groupId));
        assertEquals(200, response.getStatus());

        response = httpHelper.delete("/api/group/invite/remove", new GroupRemoveInviteRequest(authToken, inviteId1, groupId));
        assertEquals(400, response.getStatus());
    }
}
