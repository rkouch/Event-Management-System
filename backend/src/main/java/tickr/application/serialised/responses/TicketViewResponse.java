package tickr.application.serialised.responses;

import org.checkerframework.checker.index.qual.SearchIndexBottom;

import com.google.gson.annotations.SerializedName;

public class TicketViewResponse {
    @SerializedName("event_id")
    public String eventId; 

    @SerializedName("user_id")
    public String userId;

    @SerializedName("seat_num")
    public int seatNum;

    public String section;

    public TicketViewResponse(String eventId, String userId, String section, int seatNum) {
        this.eventId = eventId;
        this.userId = userId;
        this.seatNum = seatNum;
        this.section = section;
    } 

    
}
