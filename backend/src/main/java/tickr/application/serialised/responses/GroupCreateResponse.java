package tickr.application.serialised.responses;

import com.google.gson.annotations.SerializedName;

public class GroupCreateResponse {
    @SerializedName("group_id")
    public String groupId;

    public GroupCreateResponse(String groupId) {
        this.groupId = groupId;
    }

}
