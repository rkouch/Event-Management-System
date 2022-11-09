package tickr.integration.event;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import spark.Spark;
import tickr.CreateEventReqBuilder;
import tickr.application.TickrController;
import tickr.application.serialised.requests.CreateEventRequest;
import tickr.application.serialised.requests.EditHostRequest;
import tickr.application.serialised.requests.EventDeleteRequest;
import tickr.application.serialised.requests.UserRegisterRequest;
import tickr.application.serialised.responses.AuthTokenResponse;
import tickr.application.serialised.responses.CreateEventResponse;
import tickr.application.serialised.responses.EventViewResponse;
import tickr.application.serialised.responses.UserIdResponse;
import tickr.persistence.DataModel;
import tickr.persistence.HibernateModel;
import tickr.server.Server;
import tickr.util.HTTPHelper;

public class TestEventNotifications {
    private DataModel hibernateModel;
    private HTTPHelper httpHelper;

    private String eventId;
    private String authToken; 
    private String newHostAuthToken;
    private String newHostEmail = "newhost@example.com";
    private String newHostId;
    
    @BeforeEach
    public void setup () {
        hibernateModel = new HibernateModel("hibernate-test.cfg.xml");

        Server.start(8080, null, hibernateModel);
        httpHelper = new HTTPHelper("http://localhost:8080");
        Spark.awaitInitialization();

        var response = httpHelper.post("/api/user/register", new UserRegisterRequest("test", "first", "last", "test1@example.com",
                "Password123!", "2022-04-14"));
        assertEquals(200, response.getStatus());
        authToken = response.getBody(AuthTokenResponse.class).authToken;

        List<CreateEventRequest.SeatingDetails> seatingDetails = new ArrayList<>();
        seatingDetails.add(new CreateEventRequest.SeatingDetails("SectionA", 10, 50, true));
        seatingDetails.add(new CreateEventRequest.SeatingDetails("SectionB", 20, 30, true));

        response = httpHelper.post("/api/event/create", new CreateEventReqBuilder()
                .withEventName("Test Event")
                .withSeatingDetails(seatingDetails)
                .withStartDate(LocalDateTime.now().plusDays(1))
                .withEndDate(LocalDateTime.now().plusDays(2))
                .build(authToken));
        assertEquals(200, response.getStatus());
        eventId = response.getBody(CreateEventResponse.class).event_id;
    }

    @AfterEach
    public void cleanup () {
        Spark.stop();
        hibernateModel.cleanup();
        Spark.awaitStop();
    }

    @Test
    public void badRequestTests() {
        
    }
}
