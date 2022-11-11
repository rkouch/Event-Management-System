package tickr.application.serialised.requests;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class ReserveCancelRequest {
    @SerializedName("auth_token")
    public String authToken;
    public List<String> reservations = new ArrayList<>();
}
