package tickr.unit.event;

import com.google.gson.annotations.SerializedName;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import tickr.CreateEventReqBuilder;
import tickr.TestHelper;
import tickr.application.TickrController;
import tickr.application.apis.ApiLocator;
import tickr.application.apis.location.ILocationAPI;
import tickr.application.serialised.SerializedLocation;
import tickr.application.serialised.combined.EventSearch;
import tickr.application.serialised.requests.CreateEventRequest;
import tickr.application.serialised.requests.EditEventRequest;
import tickr.mock.MockLocationApi;
import tickr.persistence.DataModel;
import tickr.persistence.HibernateModel;
import tickr.persistence.ModelSession;
import tickr.server.exceptions.BadRequestException;
import tickr.server.exceptions.UnauthorizedException;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

public class TestEventSearch {
    private DataModel model;
    private TickrController controller;
    private ModelSession session;
    private String authToken;
    private MockLocationApi locationApi;
    @BeforeEach
    public void setup () {
        model = new HibernateModel("hibernate-test.cfg.xml");
        controller = new TickrController();
        locationApi = new MockLocationApi(model);
        ApiLocator.addLocator(ILocationAPI.class, () -> locationApi);

        session = model.makeSession();
        authToken = controller.userRegister(session, TestHelper.makeRegisterRequest()).authToken;
        session = TestHelper.commitMakeSession(model, session);
    }

    @AfterEach
    public void cleanup () {
        model.cleanup();
        ApiLocator.clearLocator(ILocationAPI.class);
    }

    @Test
    public void testBadRequests () {
        assertThrows(BadRequestException.class, () -> controller.searchEvents(session, Map.of()));
        assertThrows(BadRequestException.class, () -> controller.searchEvents(session, Map.of("page_start", Integer.toString(0))));
        assertThrows(BadRequestException.class, () -> controller.searchEvents(session, Map.of("max_results", Integer.toString(1))));

        assertThrows(BadRequestException.class, () -> controller.searchEvents(session,
                Map.of("page_start", Integer.toString(-1), "max_results", Integer.toString(1))));
        assertThrows(BadRequestException.class, () -> controller.searchEvents(session,
                Map.of("page_start", Integer.toString(0), "max_results", Integer.toString(0))));
        assertThrows(BadRequestException.class, () -> controller.searchEvents(session,
                Map.of("page_start", Integer.toString(0), "max_results", Integer.toString(-1))));
        assertThrows(BadRequestException.class, () -> controller.searchEvents(session,
                Map.of("page_start", Integer.toString(0), "max_results", Integer.toString(257))));
        assertThrows(BadRequestException.class, () -> controller.searchEvents(session,
                Map.of("page_start", "abcde", "max_results", Integer.toString(20))));

        assertThrows(UnauthorizedException.class, () -> controller.searchEvents(session,
                Map.of("page_start", Integer.toString(1), "max_results", Integer.toString(1), "auth_token", "testing123")));
        assertThrows(UnauthorizedException.class, () -> controller.searchEvents(session,
                Map.of("page_start", Integer.toString(1), "max_results", Integer.toString(1), "auth_token", TestHelper.makeFakeJWT())));

        assertThrows(BadRequestException.class, () -> controller.searchEvents(session,
                Map.of("page_start", Integer.toString(1), "max_results", Integer.toString(1), "search_options", "abcd%%!12398")));
        assertThrows(BadRequestException.class, () -> controller.searchEvents(session,
                Map.of("page_start", Integer.toString(1), "max_results", Integer.toString(1), "search_options",
                        Base64.getEncoder().encodeToString("testing123".getBytes()))));
    }

    @Test
    public void testNoOptions () {
        var response = makeSearch(0, 100, null);
        assertEquals(0, response.eventIds.size());
        assertEquals(0, response.numResults);

        var eventId = controller.createEvent(session, new CreateEventReqBuilder().build(authToken)).event_id;
        session = TestHelper.commitMakeSession(model, session);
        controller.editEvent(session, new EditEventRequest(eventId, authToken, null, null, null, null,
                null, null, null, null, null, null, true));
        session = TestHelper.commitMakeSession(model, session);

        response = makeSearch(0, 100, null);
        assertEquals(1, response.eventIds.size());
        assertEquals(eventId, response.eventIds.get(0));
        assertEquals(1, response.numResults);
    }

    @Test
    public void testPagination () {
        var eventIds = new ArrayList<String>();

        int numPage = 20;
        int numPages = 300 / numPage;

        for (int i = 0; i < numPages * numPage; i++) {
            eventIds.add(controller.createEvent(session, new CreateEventReqBuilder()
                    .withStartDate(LocalDateTime.now(ZoneId.of("UTC")).plus(Duration.ofDays(1 + 300 - i)))
                    .withEndDate(LocalDateTime.now(ZoneId.of("UTC")).plus(Duration.ofDays(2 + 300 - i)))
                    .build(authToken)).event_id);
            session = TestHelper.commitMakeSession(model, session);
            controller.editEvent(session, new EditEventRequest(eventIds.get(eventIds.size() - 1), authToken, null, null, null, null,
                    null, null, null, null, null, null, true));
            session = TestHelper.commitMakeSession(model, session);
        }

        for (int i = 0; i < numPages; i++) {
            var response = makeSearch(i * numPage, numPage, null);
            assertEquals(numPage, response.eventIds.size());
            assertEquals(numPages * numPage, response.numResults);
            for (int j = 0; j < numPage; j++) {
                assertEquals(eventIds.get(300 - (i * numPage + j) - 1), response.eventIds.get(j));
            }
        }

        var tooHighResponse = makeSearch(numPages * numPage, numPage, null);
        assertEquals(0, tooHighResponse.eventIds.size());
    }

    @Test
    public void testOptions () {
        var ids = createEventOptions();

        var respIds = makeSearch(0, 100, new OptionsBuilder().addCategories(List.of("music", "business")).build()).eventIds;
        assertEquals(2, respIds.size());
        assertEquals(ids.get(0), respIds.get(0));
        assertEquals(ids.get(1), respIds.get(1));

        respIds = makeSearch(0, 100, new OptionsBuilder().addStartTime(LocalDateTime.now(ZoneId.of("UTC")).plusDays(2).plusHours(1)).build()).eventIds;
        assertEquals(2, respIds.size());
        assertEquals(ids.get(0), respIds.get(0));
        assertEquals(ids.get(1), respIds.get(1));

        respIds = makeSearch(0, 100, new OptionsBuilder().addEndTime(LocalDateTime.now(ZoneId.of("UTC")).plus(Duration.ofDays(4))).build()).eventIds;
        assertEquals(2, respIds.size());
        assertEquals(ids.get(2), respIds.get(0));
        assertEquals(ids.get(0), respIds.get(1));

        respIds = makeSearch(0, 100, new OptionsBuilder().addTags(List.of("test4", "test6")).build()).eventIds;
        assertEquals(2, respIds.size());
        assertEquals(ids.get(2), respIds.get(0));
        assertEquals(ids.get(1), respIds.get(1));

        respIds = makeSearch(0, 100, new OptionsBuilder().addText("karaoke tEsTb").build()).eventIds;
        assertEquals(2, respIds.size());
        assertEquals(ids.get(0), respIds.get(0));
        assertEquals(ids.get(1), respIds.get(1));

        respIds = makeSearch(0, 100, new OptionsBuilder().addCategories(List.of("music", "business")).addText("tennis").build()).eventIds;
        assertEquals(0, respIds.size());

        respIds = makeSearch(0, 100, new OptionsBuilder()
                .addLocation(new SerializedLocation.Builder()
                        .withStreetNo(1)
                        .withStreetName("High St")
                        .withSuburb("Kensington")
                        .withPostcode("2052")
                        .withState("NSW")
                        .withCountry("Australia")
                        .build(), 10.0f)
                .build()).eventIds;
        assertEquals(2, respIds.size());
        assertEquals(ids.get(0), respIds.get(0));
        assertEquals(ids.get(1), respIds.get(1));

        respIds = makeSearch(0, 100, new OptionsBuilder()
                .addLocation(new SerializedLocation.Builder()
                        .withStreetNo(1)
                        .withStreetName("High St")
                        .withSuburb("Kensington")
                        .withPostcode("2052")
                        .withState("NSW")
                        .withCountry("Australia")
                        .build(), 260.0f)
                .build()).eventIds;
        assertEquals(3, respIds.size());
        assertEquals(ids.get(2), respIds.get(2));
        assertEquals(ids.get(0), respIds.get(0));
        assertEquals(ids.get(1), respIds.get(1));
    }

    private List<String> createEventOptions () {
        var entityIds = new ArrayList<String>();

        entityIds.add(controller.createEvent(session, new CreateEventReqBuilder()
                        .withEventName("TestA")
                .withStartDate(LocalDateTime.now(ZoneId.of("UTC")).plus(Duration.ofDays(3)))
                .withEndDate(LocalDateTime.now(ZoneId.of("UTC")).plus(Duration.ofDays(4)))
                .withCategories(Set.of("food", "music", "health"))
                .withTags(Set.of("test1", "test2", "test3"))
                .withDescription("Testing karaoke burgers hospital")
                .withLocation(new SerializedLocation.Builder()
                        .withStreetNo(1)
                        .withStreetName("High St")
                        .withSuburb("Kensington")
                        .withState("NSW")
                        .withPostcode("2052")
                        .withCountry("Australia")
                        .build())
                .build(authToken)).event_id);

        controller.editEvent(session, new EditEventRequest(entityIds.get(0), authToken, null, null, null, null,
                null, null, null, null, null, null, true));
        session = TestHelper.commitMakeSession(model, session);

        entityIds.add(controller.createEvent(session, new CreateEventReqBuilder()
                        .withEventName("TestB")
                .withStartDate(LocalDateTime.now(ZoneId.of("UTC")).plus(Duration.ofDays(5)))
                .withEndDate(LocalDateTime.now(ZoneId.of("UTC")).plus(Duration.ofDays(6)))
                .withCategories(Set.of("hobbies", "business", "free"))
                .withTags(Set.of("test4", "test2", "test5"))
                .withDescription("money capitalism free sewing")
                .withLocation(new SerializedLocation.Builder()
                        .withStreetNo(235)
                        .withStreetName("Anzac Parade")
                        .withSuburb("Kensington")
                        .withState("NSW")
                        .withPostcode("2033")
                        .withCountry("Australia")
                        .build())
                .build(authToken)).event_id);

        controller.editEvent(session, new EditEventRequest(entityIds.get(1), authToken, null, null, null, null,
                null, null, null, null, null, null, true));
        session = TestHelper.commitMakeSession(model, session);

        entityIds.add(controller.createEvent(session, new CreateEventReqBuilder()
                        .withEventName("TestC")
                .withStartDate(LocalDateTime.now(ZoneId.of("UTC")).plus(Duration.ofDays(1)))
                .withEndDate(LocalDateTime.now(ZoneId.of("UTC")).plus(Duration.ofDays(2)))
                .withCategories(Set.of("food", "education"))
                .withTags(Set.of("test2", "test5", "test6"))
                .withDescription("money burgers school")
                .withLocation(new SerializedLocation.Builder()
                        .withStreetNo(1)
                        .withStreetName("Parliament Drive")
                        .withSuburb("Canberra")
                        .withState("ACT")
                        .withPostcode("2600")
                        .withCountry("Australia")
                        .build())
                .build(authToken)).event_id);

        controller.editEvent(session, new EditEventRequest(entityIds.get(2), authToken, null, null, null, null,
                null, null, null, null, null, null, true));
        session = TestHelper.commitMakeSession(model, session);

        locationApi.awaitLocations();

        return entityIds;
    }

    private EventSearch.Response makeSearch (int pageStart, int maxResults, EventSearch.Options options) {
        var paramsMap = new HashMap<String, String>();

        paramsMap.put("page_start", Integer.toString(pageStart));
        paramsMap.put("max_results", Integer.toString(maxResults));

        if (options != null) {
            paramsMap.put("search_options", options.serialise());
        }

        var result = controller.searchEvents(session, paramsMap);
        session = TestHelper.commitMakeSession(model, session);
        return result;
    }

    private static class OptionsBuilder {
        private SerializedLocation location = null;
        private float maxDistance;

        private LocalDateTime startTime = null;
        private LocalDateTime endTime = null;

        private List<String> tags = null;
        private List<String> categories = null;

        private String text = null;

        public OptionsBuilder addLocation (SerializedLocation location, float maxDistance) {
            this.location = location;
            this.maxDistance = maxDistance;

            return this;
        }

        public OptionsBuilder addStartTime (LocalDateTime startTime) {
            this.startTime = startTime;

            return this;
        }

        public OptionsBuilder addEndTime (LocalDateTime endTime) {
            this.endTime = endTime;

            return this;
        }

        public OptionsBuilder addTags (List<String> tags) {
            this.tags = tags;

            return this;
        }

        public OptionsBuilder addCategories (List<String> categories) {
            this.categories = categories;

            return this;
        }

        public OptionsBuilder addText (String text) {
            this.text = text;

            return this;
        }


        public EventSearch.Options build () {
            return new EventSearch.Options(location, maxDistance, startTime, endTime, tags, categories, text);
        }
    }
}
