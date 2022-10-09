package tickr.integration.user;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import spark.Spark;
import tickr.TestHelper;
import tickr.application.serialised.requests.EditProfileRequest;
import tickr.application.serialised.requests.UserRegisterRequest;
import tickr.application.serialised.responses.AuthTokenResponse;
import tickr.application.serialised.responses.ViewProfileResponse;
import tickr.integration.HTTPHelper;
import tickr.persistence.DataModel;
import tickr.persistence.HibernateModel;
import tickr.server.Server;
import tickr.util.FileHelper;

import java.util.Map;

public class TestDeleteAccount {
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

    @AfterEach
    public void clearStaticFiles () {
        TestHelper.clearStaticFiles();
    }

    @Test
    public void testAccountExists () {
        
    }
}
