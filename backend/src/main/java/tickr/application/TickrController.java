package tickr.application;

import tickr.application.responses.TestResponse;
import tickr.persistence.ModelSession;

public class TickrController {
    public TickrController () {

    }

    public TestResponse testGet (ModelSession session) {
        //throw new RuntimeException("Test exception!");
        return new TestResponse("John Doe", "test@example.com");
    }

    public TestResponse testPost (ModelSession session, TestResponse.PostRequest request) {
        return new TestResponse(request.name + " :)", request.email);
    }
}
