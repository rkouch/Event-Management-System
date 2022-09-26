package tickr.application.responses;

import com.google.gson.annotations.SerializedName;
import tickr.application.entities.TestEntity;

import java.util.List;

public class TestResponses {
    public static class PostRequest {
        public String name;
        public String email;
    }

    public static class PutRequest {
        public int id;

        @SerializedName("new_name")
        public String newName;

        @SerializedName("new_email")
        public String newEmail;
    }

    public static class DeleteRequest {
        public int id;
    }

    public static class GetAll {
        public List<TestEntity> entities;

        public GetAll (List<TestEntity> entities) {
            this.entities = entities;
        }
    }

    public static class EntityResponse {
        public String name;
        public String email;
        public int id;

        public EntityResponse (String name, String email, int id) {
            this.name = name;
            this.email = email;
            this.id = id;
        }
    }
}
