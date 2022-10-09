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
}
