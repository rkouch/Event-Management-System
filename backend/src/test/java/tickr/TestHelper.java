package tickr;

import static org.junit.jupiter.api.Assertions.*;
import tickr.persistence.DataModel;
import tickr.persistence.ModelSession;

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
}
