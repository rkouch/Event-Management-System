package tickr.application.serialised.combined;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import tickr.application.serialised.SerializedLocation;
import tickr.server.exceptions.BadRequestException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class EventSearch {
    public static class Options {
        public SerializedLocation location = null;

        @SerializedName("start_time")
        private String startTime = null;
        @SerializedName("end_time")
        private String endTime = null;

        public List<String> tags = new ArrayList<>();
        public List<String> categories = new ArrayList<>();

        public String text = null;

        public Options () {

        }

        public Options (SerializedLocation location, LocalDateTime startTime, LocalDateTime endTime, List<String> tags, List<String> categories, String text) {
            this.location = location;
            this.startTime = startTime.format(DateTimeFormatter.ISO_DATE_TIME);
            this.endTime = endTime.format(DateTimeFormatter.ISO_DATE_TIME);
            this.tags = tags;
            this.categories = categories;
            this.text = text;
        }

        public LocalDateTime getStartTime () {
            if (startTime == null) {
                return null;
            }
            try {
                return LocalDateTime.parse(startTime, DateTimeFormatter.ISO_DATE_TIME);
            } catch (DateTimeParseException e) {
                throw new BadRequestException("Invalid start time: " + startTime, e);
            }
        }

        public LocalDateTime getEndTime () {
            if (endTime == null) {
                return null;
            }
            try {
                return LocalDateTime.parse(endTime, DateTimeFormatter.ISO_DATE_TIME);
            } catch (DateTimeParseException e) {
                throw new BadRequestException("Invalid end time: " + endTime, e);
            }
        }

        public String serialise () {
            return Base64.getEncoder().encodeToString(new Gson().toJson(this).getBytes());
        }
    }

    public static class Response {
        @SerializedName("event_ids")
        public List<String> eventIds;

        @SerializedName("num_results")
        public int numResults;

        public Response () {

        }

        public Response (List<String> eventIds, int numResults) {
            this.eventIds = eventIds;
            this.numResults = numResults;
        }
    }

    public Options fromParams (String queryString) {
        return new Gson().fromJson(new String(Base64.getDecoder().decode(queryString)), Options.class);
    }
}
