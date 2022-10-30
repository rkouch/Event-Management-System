package tickr.application.serialised.requests;

import java.util.ArrayList;
import java.util.List;

public class ReserveCancelRequest {
    public String authToken;
    public List<String> reservations = new ArrayList<>();
}
