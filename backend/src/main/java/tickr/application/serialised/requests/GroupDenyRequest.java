package tickr.application.serialised.requests;

import com.google.gson.annotations.SerializedName;

public class GroupDenyRequest {
    @SerializedName("invite_id")
    public String inviteId;

    public GroupDenyRequest(String inviteId) {
        this.inviteId = inviteId;
    }

    
}
