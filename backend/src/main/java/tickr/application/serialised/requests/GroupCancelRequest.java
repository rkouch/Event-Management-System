package tickr.application.serialised.requests;

import com.google.gson.annotations.SerializedName;

public class GroupCancelRequest {
    @SerializedName("auth_token")
    public String authToken;

    @SerializedName("gorup_id")
    public String groupId;

    public GroupCancelRequest(String authToken, String groupId) {
        this.authToken = authToken;
        this.groupId = groupId;
    }

    
}
