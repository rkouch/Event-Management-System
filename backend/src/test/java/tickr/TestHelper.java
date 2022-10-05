package tickr;

import static org.junit.jupiter.api.Assertions.*;

import tickr.application.serialised.requests.UserRegisterRequest;
import tickr.persistence.DataModel;
import tickr.persistence.ModelSession;
import tickr.util.CryptoHelper;

import java.sql.Date;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

public class TestHelper {
    public static ModelSession commitMakeSession (DataModel model, ModelSession session) {
        assertDoesNotThrow(session::commit);
        session.close();

        return model.makeSession();
    }

    public static void sleep (long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public static String makeFakeJWT (UUID userId) {
        return CryptoHelper.makeJWTBuilder()
                .setId(UUID.randomUUID().toString())
                .setSubject(userId.toString())
                .setIssuedAt(Date.from(Instant.now()))
                .setExpiration(Date.from(Instant.now().plus(Duration.ofDays(30))))
                .compact();
    }

    public static String makeFakeJWT () {
        return makeFakeJWT(UUID.randomUUID());
    }

    public static UserRegisterRequest makeRegisterRequest () {
        var user = "x" + Long.toHexString(System.nanoTime());
        return new UserRegisterRequest(user, "First", "Last", user + "@example.com", "Password123!", "2022-01-01");
    }
}
