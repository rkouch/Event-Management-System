package tickr;

import tickr.application.serialised.SerializedLocation;
import tickr.application.serialised.requests.CreateEventRequest;
import tickr.util.FileHelper;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CreateEventReqBuilder {
    private String authTokenString;
    private String eventName = "testing";
    private String picture = null;
    private SerializedLocation location = new SerializedLocation.Builder().build();
    private LocalDateTime startDate = LocalDateTime.now(ZoneId.of("UTC")).plus(Duration.ofDays(1));
    private LocalDateTime endDate = LocalDateTime.now(ZoneId.of("UTC")).plus(Duration.ofDays(1)).plus(Duration.ofHours(1));
    private String description = "";
    private List<CreateEventRequest.SeatingDetails> seatingDetails = new ArrayList<>();
    private Set<String> admins = new HashSet<>();
    private Set<String> categories = new HashSet<>();
    private Set<String> tags = new HashSet<>();

    public CreateEventReqBuilder withEventName (String eventName) {
        this.eventName = eventName;
        return this;
    }
    public CreateEventReqBuilder withPicture (String pictureLocation) {
        this.picture = FileHelper.readToDataUrl(pictureLocation);
        return this;
    }
    public CreateEventReqBuilder withLocation (SerializedLocation location) {
        this.location = location;
        return this;
    }
    public CreateEventReqBuilder withStartDate (LocalDateTime startDate) {
        this.startDate = startDate;
        return this;
    }
    public CreateEventReqBuilder withEndDate (LocalDateTime endDate) {
        this.endDate = endDate;
        return this;
    }
    public CreateEventReqBuilder withDescription (String description) {
        this.description = description;
        return this;
    }
    public CreateEventReqBuilder withSeatingDetails (List<CreateEventRequest.SeatingDetails> seatingDetails) {
        this.seatingDetails = seatingDetails;
        return this;
    }
    public CreateEventReqBuilder withAdmins (Set<String> admins) {
        this.admins = admins;
        return this;
    }
    public CreateEventReqBuilder withCategories (Set<String> categories) {
        this.categories = categories;
        return this;
    }
    public CreateEventReqBuilder withTags (Set<String> tags) {
        this.tags = tags;
        return this;
    }

    public CreateEventRequest build (String authToken) {
        return new CreateEventRequest(authToken, eventName, picture, location, startDate.format(DateTimeFormatter.ISO_DATE_TIME),
                endDate.format(DateTimeFormatter.ISO_DATE_TIME), description, seatingDetails, admins, categories, tags);
    }

    public String getEventName() {
        return eventName;
    }
    public void setEventName(String eventName) {
        this.eventName = eventName;
    }
    public String getPicture() {
        return picture;
    }
    public void setPicture(String picture) {
        this.picture = picture;
    }
    public SerializedLocation getLocation() {
        return location;
    }
    public void setLocation(SerializedLocation location) {
        this.location = location;
    }
    public LocalDateTime getStartDate() {
        return startDate;
    }
    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }
    public LocalDateTime getEndDate() {
        return endDate;
    }
    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public List<CreateEventRequest.SeatingDetails> getSeatingDetails() {
        return seatingDetails;
    }
    public void setSeatingDetails(List<CreateEventRequest.SeatingDetails> seatingDetails) {
        this.seatingDetails = seatingDetails;
    }
    public Set<String> getAdmins() {
        return admins;
    }
    public void setAdmins(Set<String> admins) {
        this.admins = admins;
    }
    public Set<String> getCategories() {
        return categories;
    }
    public void setCategories(Set<String> categories) {
        this.categories = categories;
    }
    public Set<String> getTags() {
        return tags;
    }
    public void setTags(Set<String> tags) {
        this.tags = tags;
    }

    public String getAuthTokenString() {
        return authTokenString;
    }

    public void setAuthTokenString(String authTokenString) {
        this.authTokenString = authTokenString;
    }
    
    
}
