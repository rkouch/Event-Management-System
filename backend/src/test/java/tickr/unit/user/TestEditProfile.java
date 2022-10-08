package tickr.unit.user;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import tickr.TestHelper;
import tickr.application.TickrController;
import tickr.application.serialised.requests.EditProfileRequest;
import tickr.application.serialised.requests.UserRegisterRequest;
import tickr.persistence.DataModel;
import tickr.persistence.HibernateModel;
import tickr.persistence.ModelSession;
import tickr.server.exceptions.BadRequestException;
import tickr.server.exceptions.ForbiddenException;
import tickr.server.exceptions.UnauthorizedException;

import java.util.Map;

public class TestEditProfile {
    private DataModel model;
    private TickrController controller;

    private ModelSession session;
    private String authToken;
    @BeforeEach
    public void setup () {
        model = new HibernateModel("hibernate-test.cfg.xml");
        controller = new TickrController();

        session = model.makeSession();
        authToken = controller.userRegister(session, new UserRegisterRequest("TestUsername", "Test", "User", "test@example.com",
                "Password123!", "2010-10-07")).authToken;
        session = TestHelper.commitMakeSession(model, session);
    }

    @AfterEach
    public void cleanup () {
        model.cleanup();
    }

    @Test
    public void testBadRequest () {
        assertThrows(BadRequestException.class, () -> controller.userEditProfile(session,
                new EditProfileRequest(null, null, null, null, null, null, null)));

        assertThrows(UnauthorizedException.class, () -> controller.userEditProfile(session,
                new EditProfileRequest(TestHelper.makeFakeJWT(), null, null, null, null, "test@", null)));

        assertThrows(ForbiddenException.class, () -> controller.userEditProfile(session,
                new EditProfileRequest(authToken, null, null, null, null, "test@", null)));
        assertThrows(ForbiddenException.class, () -> controller.userEditProfile(session,
                new EditProfileRequest(authToken, null, null, null, null, "@example.com", null)));
        assertThrows(ForbiddenException.class, () -> controller.userEditProfile(session,
                new EditProfileRequest(authToken, null, null, null, null, "test@example", null)));
    }

    @Test
    public void testNoChanges () {
        assertDoesNotThrow(() -> controller.userEditProfile(session,
                new EditProfileRequest(authToken, null, null, null, null, null, null)));
        session = TestHelper.commitMakeSession(model, session);
        var response = controller.userGetProfile(session, Map.of("auth_token", authToken));

        assertEquals("TestUsername", response.userName);
        assertEquals("Test", response.firstName);
        assertEquals("User", response.lastName);
        assertEquals("test@example.com", response.email);
        assertEquals("", response.description);
        assertEquals("", response.profilePicture);
    }

    @Test
    public void testChangesNoPfp () {
        assertDoesNotThrow(() -> controller.userEditProfile(session,
                new EditProfileRequest(authToken, "TestNew", "John", "Doe", null, "test2@gmail.com", "testing123")));

        session = TestHelper.commitMakeSession(model, session);
        var response = controller.userGetProfile(session, Map.of("auth_token", authToken));

        assertEquals("TestNew", response.userName);
        assertEquals("John", response.firstName);
        assertEquals("Doe", response.lastName);
        assertEquals("test2@gmail.com", response.email);
        assertEquals("testing", response.description);
        assertEquals("", response.profilePicture);
    }
}
