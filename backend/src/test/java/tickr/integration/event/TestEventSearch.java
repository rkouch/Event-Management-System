package tickr.integration.event;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import spark.Spark;
import tickr.CreateEventReqBuilder;
import tickr.TestHelper;
import tickr.application.serialised.combined.EventSearch;
import tickr.application.serialised.requests.UserRegisterRequest;
import tickr.application.serialised.responses.AuthTokenResponse;
import tickr.application.serialised.responses.CreateEventResponse;
import tickr.integration.HTTPHelper;
import tickr.persistence.DataModel;
import tickr.persistence.HibernateModel;
import tickr.server.Server;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestEventSearch {
    private DataModel hibernateModel;
    private HTTPHelper httpHelper;
    private String authToken;

    @BeforeEach
    public void setup () {
        hibernateModel = new HibernateModel("hibernate-test.cfg.xml");

        Server.start(8080, null, hibernateModel);
        httpHelper = new HTTPHelper("http://localhost:8080");
        Spark.awaitInitialization();

        var response = httpHelper.post("/api/user/register", new UserRegisterRequest("TestUsername", "Test", "User", "test@example.com",
                "Password123!", "2010-10-07"));
        assertEquals(200, response.getStatus());
        authToken = response.getBody(AuthTokenResponse.class).authToken;
    }

    @AfterEach
    public void cleanup () {
        Spark.stop();
        hibernateModel.cleanup();
        Spark.awaitStop();
    }

    @Test
    public void testNoOptions () {
        var searchResponse = makeSearch(0, 100, null);
        assertEquals(0, searchResponse.numResults);


        var response = httpHelper.post("/api/event/create", new CreateEventReqBuilder().build(authToken));
        assertEquals(200, response.getStatus());
        var eventId = response.getBody(CreateEventResponse.class).event_id;

        searchResponse = makeSearch(0, 100, null);
        assertEquals(1, searchResponse.numResults);
        assertEquals(eventId, searchResponse.eventIds.get(0));
    }



    private EventSearch.Response makeSearch (int pageStart, int maxResults, EventSearch.Options options) {
        var paramsMap = new HashMap<String, String>();

        paramsMap.put("page_start", Integer.toString(pageStart));
        paramsMap.put("max_results", Integer.toString(maxResults));

        if (options != null) {
            paramsMap.put("search_options", options.serialise());
        }

        //var response = controller.searchEvents(session, paramsMap);
        var response = httpHelper.get("/api/event/search", paramsMap);
        assertEquals(200, response.getStatus());

        return response.getBody(EventSearch.Response.class);
    }
}
