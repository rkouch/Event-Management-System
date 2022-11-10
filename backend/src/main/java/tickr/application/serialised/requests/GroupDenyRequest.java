package tickr.application.serialised.requests;

import com.google.gson.annotations.SerializedName;

public class GroupDenyRequest {
    @SerializedName("invite_id")
    public String invideId;

    public GroupDenyRequest(String invideId) {
        this.invideId = invideId;
    }

    
}
