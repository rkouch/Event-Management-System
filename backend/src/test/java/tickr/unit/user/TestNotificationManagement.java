package tickr.unit.user;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import tickr.TestHelper;
import tickr.application.TickrController;
import tickr.application.entities.AuthToken;
import tickr.application.entities.User;
import tickr.application.serialised.combined.NotificationManagement;
import tickr.application.serialised.requests.UserRegisterRequest;
import tickr.persistence.DataModel;
import tickr.persistence.HibernateModel;
import tickr.server.exceptions.BadRequestException;
import tickr.util.CryptoHelper;

import java.sql.Date;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

public class TestNotificationManagement {
    private DataModel model;
    private TickrController controller;
    @BeforeEach
    public void setup () {
        model = new HibernateModel("hibernate-test.cfg.xml");
        controller = new TickrController();
    }

    @AfterEach
    public void cleanup () {
        model.cleanup();
    }

    @Test
    public void testBadRequests () {
        var session = model.makeSession();
        var user = new User("test@example.com", "Testing123!", "test", "first", "last", LocalDate.of(2020, 8, 14));
        session.save(user);
        session = TestHelper.commitMakeSession(model, session);

        var finalSession = session;
        assertThrows(BadRequestException.class, () -> controller.userGetSettings(finalSession, Map.of()));
        assertThrows(BadRequestException.class, () -> controller.userUpdateSettings(finalSession, new NotificationManagement.UpdateRequest()));

        assertThrows(BadRequestException.class, () -> controller.userGetSettings(finalSession, Map.of("auth_token", "testing123")));
        assertThrows(BadRequestException.class, () -> controller.userUpdateSettings(finalSession, new NotificationManagement.UpdateRequest("testing123",
                new NotificationManagement.Settings())));

        assertThrows(BadRequestException.class, () -> controller.userGetSettings(finalSession, Map.of("auth_token", TestHelper.makeFakeJWT(user.getId()))));
        assertThrows(BadRequestException.class, () -> controller.userUpdateSettings(finalSession, new NotificationManagement.UpdateRequest(TestHelper.makeFakeJWT(user.getId()),
                new NotificationManagement.Settings())));

        var expiredToken = new AuthToken(user, LocalDateTime.now().minus(Duration.ofDays(1)), Duration.ofHours(1));
        session.save(expiredToken);
        session = TestHelper.commitMakeSession(model, session);
        var expiredStr = expiredToken.makeJWT();

        var finalSession1 = session;
        assertThrows(BadRequestException.class, () -> controller.userGetSettings(finalSession1, Map.of("auth_token", expiredStr)));
        assertThrows(BadRequestException.class, () -> controller.userUpdateSettings(finalSession1, new NotificationManagement.UpdateRequest(expiredStr,
                new NotificationManagement.Settings())));
    }

    @Test
    public void testDefaultGet () {
        var session = model.makeSession();
        var authToken = controller.userRegister(session, TestHelper.makeRegisterRequest()).authToken;
        session = TestHelper.commitMakeSession(model, session);

        assertTrue(controller.userGetSettings(session, Map.of("auth_token", authToken)).settings.reminders);
    }

    @Test
    public void testSettingsSet () {
        var session = model.makeSession();
        var authToken = controller.userRegister(session, TestHelper.makeRegisterRequest()).authToken;
        session = TestHelper.commitMakeSession(model, session);

        var updateSettings = controller.userUpdateSettings(session, new NotificationManagement.UpdateRequest(authToken,
                new NotificationManagement.Settings(false))).settings;
        session = TestHelper.commitMakeSession(model, session);
        var getSettings = controller.userGetSettings(session, Map.of("auth_token", authToken)).settings;
        session = TestHelper.commitMakeSession(model, session);

        assertFalse(updateSettings.reminders);
        assertFalse(getSettings.reminders);

        var updateSettings2 = controller.userUpdateSettings(session, new NotificationManagement.UpdateRequest(authToken,
                new NotificationManagement.Settings(true))).settings;
        session = TestHelper.commitMakeSession(model, session);
        var getSetting2 = controller.userGetSettings(session, Map.of("auth_token", authToken)).settings;
        session = TestHelper.commitMakeSession(model, session);

        assertTrue(updateSettings2.reminders);
        assertTrue(getSetting2.reminders);
    }

    @Test
    public void testEmptySettings () {
        var session = model.makeSession();
        var authToken = controller.userRegister(session, TestHelper.makeRegisterRequest()).authToken;
        session = TestHelper.commitMakeSession(model, session);

        var settings = new NotificationManagement.Settings();
        settings.reminders = null;

        var updateSettings = controller.userUpdateSettings(session, new NotificationManagement.UpdateRequest(authToken,
                settings)).settings;
        session = TestHelper.commitMakeSession(model, session);
        assertTrue(updateSettings.reminders);

        controller.userUpdateSettings(session, new NotificationManagement.UpdateRequest(authToken,
                new NotificationManagement.Settings(false)));
        session = TestHelper.commitMakeSession(model, session);

        var updateSettings2 = controller.userUpdateSettings(session, new NotificationManagement.UpdateRequest(authToken,
                settings)).settings;
        session = TestHelper.commitMakeSession(model, session);
        assertFalse(updateSettings2.reminders);
    }
}
