package tickr.application.serialised.responses;

import java.util.List;
import java.util.Set;

import com.google.gson.annotations.SerializedName;

import tickr.application.serialised.SerializedLocation;

public class EventViewResponse {
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

    public EventViewResponse () {}

    public EventViewResponse(String eventName, String picture, SerializedLocation location, String startDate,
            String endDate, String description, List<SeatingDetails> seatingDetails, Set<String> admins,
            Set<String> categories, Set<String> tags) {
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

    
}