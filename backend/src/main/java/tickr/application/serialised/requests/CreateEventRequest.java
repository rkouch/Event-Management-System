package tickr.application.serialised.requests;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import com.google.gson.annotations.SerializedName;

import tickr.application.entities.Category;
import tickr.application.entities.Location;
import tickr.application.entities.Tag;
import tickr.application.entities.Location;
import tickr.application.entities.User;
import tickr.application.serialised.SerializedLocation;

public class CreateEventRequest {
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

      
    }

    public CreateEventRequest (String authToken, String eventName, String picture, SerializedLocation location, String startDate, 
    String endDate, String description, List<SeatingDetails> seatingDetails, Set<String> admins, Set<String> categories, Set<String> tags) {
        this.authToken = authToken;
        this.eventName = eventName;
        this.picture = picture; 
        this.location = location;
        this.startDate = startDate; 
        this.endDate = endDate; 
        this.description = description; 
        this.seatingDetails = seatingDetails; 
        this.admins = admins;
        this.categories = categories;
        this.tags = tags; 
    }

    public boolean isValid() {
        return authToken != null && !authToken.isEmpty() && eventName != null && !eventName.isEmpty()
                && location != null &&  startDate != null && endDate != null && admins != null && categories != null && tags != null;
    }

    public boolean isSeatingDetailsValid() {
        for (SeatingDetails seats : seatingDetails) {
            if (seats.section == null || seats.section.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    public boolean isLocationValid() {
        return location.postcode != null && location.state != null && location.country != null && location.longitude != null && location.latitude != null;
    }

    public int getSeatAvailability() {
        int count = 0;
        for (SeatingDetails details : seatingDetails) {
            count += details.availability;
        }
        return count; 
    }
}   
