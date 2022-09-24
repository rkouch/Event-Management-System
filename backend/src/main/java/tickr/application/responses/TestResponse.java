package tickr.application.responses;

public class TestResponse {
    public String name;
    public String email;

    public TestResponse (String name, String email) {
        this.name = name;
        this.email = email;
    }

    public static class PostRequest {
        public String name;
        public String email;
    }
}
