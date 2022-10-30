package tickr.application.serialised.responses;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.annotations.SerializedName;

import tickr.application.entities.Event;

public class CustomerEventsResponse {
    public List<Bookings> bookings; 

    static public class Bookings {
        @SerializedName("event_id")
        public String eventId; 

        @SerializedName("ticket_ids")
        public List<String> ticketIds = new ArrayList<>();

        public Bookings(String eventId, List<String> ticketIds) {
            this.eventId = eventId;
            this.ticketIds = ticketIds;
        }    

        public Bookings(String eventId) {
            this.eventId = eventId;
        }

        public void addTicketId(String ticketId) {
            this.ticketIds.add(ticketId);
        }
    }

    public CustomerEventsResponse(List<Bookings> bookings) {
        this.bookings = bookings;
    }
}
