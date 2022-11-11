package tickr.application.serialised.responses;

import com.google.gson.annotations.SerializedName;

public class GroupAcceptResponse {
    @SerializedName("reserve_id")
    public String reserveId;

    public GroupAcceptResponse(String reserveId) {
        this.reserveId = reserveId;
    }

}
