package tickr.application.serialised.requests;

import java.time.LocalDateTime;
import java.util.List;

import com.google.gson.annotations.SerializedName;

import tickr.application.entities.Location;
import tickr.application.serialised.SerializedLocation;

public class CreateEventRequest {
    public static final String SeatingDetails = null;

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

    public static class SeatingDetails {
        public String section;
        public int availability; 
        public SeatingDetails(String section, int availability) {
            this.section = section;
            this.availability = availability;
        }

        public boolean isEmpty() {
            return section.isEmpty() || availability == 0;
        }
    }

    public CreateEventRequest (String authToken, String eventName, String picture, SerializedLocation location, String startDate, 
    String endDate, String description, List<SeatingDetails> seatingDetails) {
        this.authToken = authToken;
        this.eventName = eventName;
        this.picture = picture; 
        this.location = location;
        this.startDate = startDate; 
        this.endDate = endDate; 
        this.description = description; 
        this.seatingDetails = seatingDetails; 
    }

    public boolean isValid() {
        return authToken != null && !authToken.isEmpty() && eventName != null && !eventName.isEmpty() && picture != null && !picture.isEmpty()
                && location != null &&  startDate != null && endDate != null;
    }

    public int getSeatAvailability() {
        int count = 0;
        for (SeatingDetails details : seatingDetails) {
            count += details.availability;
        }
        return count; 
    }

}   
