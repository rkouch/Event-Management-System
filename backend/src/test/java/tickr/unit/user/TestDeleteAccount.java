package tickr.unit.user;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import tickr.TestHelper;
import tickr.application.TickrController;
import tickr.application.serialised.requests.EditProfileRequest;
import tickr.application.serialised.requests.UserDeleteRequest;
import tickr.application.serialised.requests.UserRegisterRequest;
import tickr.persistence.DataModel;
import tickr.persistence.HibernateModel;
import tickr.persistence.ModelSession;
import tickr.server.exceptions.BadRequestException;
import tickr.server.exceptions.ForbiddenException;
import tickr.server.exceptions.UnauthorizedException;
import tickr.util.FileHelper;

import java.util.Map;

public class TestDeleteAccount {
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

    @AfterAll
    public static void clearStaticFiles () {
        TestHelper.clearStaticFiles();
    }

    @Test
    public void testBadRequests () {
        assertThrows(UnauthorizedException.class, () -> controller.userDeleteAccount(session,
                new UserDeleteRequest(null, "Password123!")));
        assertThrows(UnauthorizedException.class, () -> controller.userDeleteAccount(session,
                new UserDeleteRequest(TestHelper.makeFakeJWT(), "Password123!")));

        assertThrows(BadRequestException.class, () -> controller.userDeleteAccount(session,
                new UserDeleteRequest(authToken, "WrongPassword123!")));
        assertThrows(BadRequestException.class, () -> controller.userDeleteAccount(session,
                new UserDeleteRequest(authToken, null)));
        
    }

    @Test
    public void testAccountExists () {
        assertDoesNotThrow(() -> controller.userDeleteAccount(session, new UserDeleteRequest(authToken, "Password123!")));
        //assertThrows(ForbiddenException.class, () -> controller.useDeleteAccount(session, new UserDeleteRequest(authToken, ) ));
    }

    @Test
    public void testAuthTokenDeleted () {
        session = TestHelper.commitMakeSession(model, session);
        var response = controller.userDeleteAccount(session, new UserDeleteRequest(authToken, "Password123!"));
        
    }
}
