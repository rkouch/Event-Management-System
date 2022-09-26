package tickr.application.responses;

import com.google.gson.annotations.SerializedName;
import tickr.application.entities.TestEntity;

import java.util.List;

public class TestResponses {
    public static class PostRequest {
        public String name;
        public String email;

        public PostRequest () {

        }

        public PostRequest (String name, String email) {
            this.name = name;
            this.email = email;
        }
    }

    public static class PutRequest {
        public int id;

        @SerializedName("new_name")
        public String newName;

        @SerializedName("new_email")
        public String newEmail;

        public PutRequest () {

        }

        public PutRequest (int id, String newName, String newEmail) {
            this.id = id;
            this.newName = newName;
            this.newEmail = newEmail;
        }
    }

    public static class DeleteRequest {
        public int id;

        public DeleteRequest (int id) {
            this.id = id;
        }
    }

    public static class GetAll {
        public List<TestEntity> entities;

        public GetAll (List<TestEntity> entities) {
            this.entities = entities;
        }
    }
}
