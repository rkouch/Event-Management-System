package tickr.integration.user;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import spark.Spark;
import tickr.application.entities.ResetToken;
import tickr.application.serialised.requests.UserChangePasswordRequest;
import tickr.application.serialised.requests.UserCompleteChangePasswordRequest;
import tickr.application.serialised.requests.UserLoginRequest;
import tickr.application.serialised.requests.UserLogoutRequest;
import tickr.application.serialised.requests.UserRegisterRequest;
import tickr.application.serialised.requests.UserRequestChangePasswordRequest;
import tickr.application.serialised.responses.AuthTokenResponse;
import tickr.application.serialised.responses.RequestChangePasswordResponse;
import tickr.integration.HTTPHelper;
import tickr.persistence.DataModel;
import tickr.persistence.HibernateModel;
import tickr.server.Server;

public class TestChangePassword {
    private DataModel hibernateModel;
    private HTTPHelper httpHelper;

    private String authToken;
    private String resetToken;
    @BeforeEach
    public void setup () {
        hibernateModel = new HibernateModel("hibernate-test.cfg.xml");

        Server.start(8080, null, hibernateModel);
        httpHelper = new HTTPHelper("http://localhost:8080");
        Spark.awaitInitialization();

        var response = httpHelper.post("/api/user/register", new UserRegisterRequest("test_username", "TestFirst",
                "TestLast", "test@example.com", "Testing123!", "2022-04-14"));
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
    public void testBadEmail () {
        assertEquals(400, httpHelper.post("/api/user/reset/request", new UserRequestChangePasswordRequest(null)).getStatus());

        assertEquals(403, httpHelper.post("/api/user/reset/request", new UserRequestChangePasswordRequest("test")).getStatus());

        assertEquals(403, httpHelper.post("/api/user/reset/request", new UserRequestChangePasswordRequest("test@")).getStatus());

        assertEquals(403, httpHelper.post("/api/user/reset/request", new UserRequestChangePasswordRequest("test@example")).getStatus());

        assertEquals(403, httpHelper.post("/api/user/reset/request", new UserRequestChangePasswordRequest("@example.com")).getStatus());
    }

    @Test void testNonexistentEmail () {
        assertEquals(403, httpHelper.post("/api/user/reset/request", new UserRequestChangePasswordRequest("test1@example.com")).getStatus());

        assertEquals(403, httpHelper.post("/api/user/reset/request", new UserRequestChangePasswordRequest("test2@example.com")).getStatus());
    }

    @Test
    public void testWrongPassword () {
        assertEquals(403, httpHelper.put("/api/user/reset", new UserChangePasswordRequest("Wrongone123!", "Newpassword123!", authToken)).getStatus());

        assertEquals(400, httpHelper.put("/api/user/reset", new UserChangePasswordRequest(null, "Newpassword123!", authToken)).getStatus());

    }

    @Test
    public void testBadNewPassword () {
        assertEquals(400, httpHelper.put("/api/user/reset", new UserChangePasswordRequest("Testing123!", null, authToken)).getStatus());

        assertEquals(403, httpHelper.put("/api/user/reset", new UserChangePasswordRequest("Testing123!", "aA1!", authToken)).getStatus());

        assertEquals(403, httpHelper.put("/api/user/reset", new UserChangePasswordRequest("Testing123!", "AAAAAAAA1!", authToken)).getStatus());

        assertEquals(403, httpHelper.put("/api/user/reset", new UserChangePasswordRequest("Testing123!", "aaaaaaaa1!", authToken)).getStatus());

        assertEquals(403, httpHelper.put("/api/user/reset", new UserChangePasswordRequest("Testing123!", "aaaaaaaaA!", authToken)).getStatus());

        assertEquals(403, httpHelper.put("/api/user/reset", new UserChangePasswordRequest("Testing123!", "aaaaaaaaA1", authToken)).getStatus());
    }

    @Test
    public void testLoggedChangePassword () {
        var response = httpHelper.put("/api/user/reset", new UserChangePasswordRequest("Testing123!", "Newpassword123!", authToken));
        assertEquals(200, response.getStatus());

        response = httpHelper.delete("/api/user/logout", new UserLogoutRequest(authToken));
        assertEquals(200, response.getStatus());
        
        assertEquals(200, httpHelper.post("/api/user/login",
                new UserLoginRequest("test@example.com", "Newpassword123!")).getStatus());
    }

    @Test
    public void testNoMatchingResetTokenChangePassword () {
        var response = httpHelper.post("/api/user/reset/request", new UserRequestChangePasswordRequest("test@example.com"));
        assertEquals(200, response.getStatus());
        var resetTokenString = response.getBody(ResetToken.class).getId();

        var response2 = httpHelper.post("/api/user/register", new UserRegisterRequest("test_username1", "TestFirst1",
                "TestLast1", "test1@example.com", "Testing123!", "2022-03-14"));
        
        assertEquals(200, httpHelper.post("/api/user/reset/request", new UserRequestChangePasswordRequest("test1@example.com")).getStatus());
        var resetTokenString2 = response2.getBody(ResetToken.class).getId();
        assertEquals(resetTokenString, null);

        //assertNotEquals(resetTokenString, resetTokenString2);
        var completeResponse = httpHelper.put("/api/user/reset/complete", new UserCompleteChangePasswordRequest("test@example.com", "Testing123!", resetTokenString.toString()));
        var sent = completeResponse.getBody(RequestChangePasswordResponse.class).success;
        assertEquals(true, sent);
    }

    @Test
    public void testMatchingResetTokenChangePassword () {
        var response = httpHelper.post("/api/user/reset/complete", new UserCompleteChangePasswordRequest())
    }
}
