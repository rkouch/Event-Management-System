package tickr.unit.user;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;
import tickr.TestHelper;
import tickr.application.TickrController;
import tickr.application.apis.ApiLocator;
import tickr.application.apis.email.IEmailAPI;
import tickr.application.entities.ResetToken;
import tickr.application.entities.User;
import tickr.application.serialised.requests.UserChangePasswordRequest;
import tickr.application.serialised.requests.UserCompleteChangePasswordRequest;
import tickr.application.serialised.requests.UserRegisterRequest;
import tickr.application.serialised.requests.UserRequestChangePasswordRequest;
import tickr.mock.MockEmailAPI;
import tickr.persistence.DataModel;
import tickr.persistence.HibernateModel;
import tickr.persistence.ModelSession;
import tickr.server.exceptions.BadRequestException;
import tickr.server.exceptions.ForbiddenException;
import tickr.server.exceptions.UnauthorizedException;
import tickr.util.CryptoHelper;
import tickr.util.FileHelper;

import java.util.Map;
import java.util.UUID;


public class TestChangePassword {
    private DataModel model;
    private TickrController controller;

    private ModelSession session;
    private String authToken;
    @BeforeAll
    public static void setupApi () {
        ApiLocator.addLocator(IEmailAPI.class, MockEmailAPI::new);
    }
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

    @AfterAll
    public static void cleanupApis () {
        ApiLocator.resetLocators();
    }

    @Test
    public void testBadRequest() {
        assertThrows(BadRequestException.class, () -> controller.loggedChangePassword(session,
                new UserChangePasswordRequest("Password123!", "NewPassword123!", null)));
        assertThrows(UnauthorizedException.class, () -> controller.loggedChangePassword(session,
                new UserChangePasswordRequest("Password123!", "NewPassword123!", TestHelper.makeFakeJWT())));
        assertThrows(BadRequestException.class, () -> controller.loggedChangePassword(session,
                new UserChangePasswordRequest("Password123!", null, authToken)));
        assertThrows(BadRequestException.class, () -> controller.loggedChangePassword(session,
                new UserChangePasswordRequest(null, "Password123!", authToken)));
   }

    @Test
    public void testResetToken () {
        var session = model.makeSession();
        controller.unloggedChangePassword(session, new UserRequestChangePasswordRequest("test@example.com"));
        session = TestHelper.commitMakeSession(model, session);

        var user = session.getByUnique(User.class, "email", "test@example.com").orElse(null);
        var list = session.getAllWith(ResetToken.class, "user", user);
        assertEquals(1, list.size());
        
        session = TestHelper.commitMakeSession(model, session);
        var resetToken = session;
        assertDoesNotThrow(() -> controller.unloggedComplete(resetToken,
                new UserCompleteChangePasswordRequest("test@example.com", "NewPassword123!", list.get(0).toString())));
        assertThrows(BadRequestException.class, () -> controller.unloggedComplete(resetToken,
                new UserCompleteChangePasswordRequest("test@example.com", "NewPassword123!", null)));
    }
}
