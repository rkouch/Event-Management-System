package tickr.application.serialised.requests;

import java.util.List;

import com.google.gson.annotations.SerializedName;

public class EditEventRequest {
    @SerializedName("auth_token")
    public String authToken; 

    @SerializedName("event_name")
    public String eventName; 

    public String picture;

    public SerializedLocation location; 

    @SerializedName("start_date")
    public String startDate;

    @SerializedName("end_date")
    public String endDate;

    public String description; 

    @SerializedName("seating_details")
    public List<SeatingDetails> seatingDetails; 

    public Set<String> admins; 

    public Set<String> categories;

    public Set<String> tags;

    public static class SeatingDetails {
        public String section;
        public int availability; 
        public SeatingDetails(String section, int availability) {
            this.section = section;
            this.availability = availability;
        }
        
    public EditEventRequest () {}


}
