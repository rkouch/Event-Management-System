package tickr.application.serialised.combined;

import com.google.gson.annotations.SerializedName;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class TicketReserve {
    public static class Request {
        @SerializedName("auth_token")
        public String authToken = null;

        @SerializedName("event_id")
        public String eventId = null;

        @SerializedName("ticket_datetime")
        public String ticketDateTime = null;

        @SerializedName("ticket_details")
        public List<TicketDetails> ticketDetails = null;

        public Request () {

        }

        public Request (String authToken, String eventId, String ticketDateTime, List<TicketDetails> ticketDetails) {
            this.authToken = authToken;
            this.ticketDateTime = ticketDateTime;
            this.ticketDetails = ticketDetails;
        }

        public Request (String authToken, String eventId, LocalDateTime ticketDateTime, List<TicketDetails> ticketDetails) {
            this(authToken, eventId, ticketDateTime.format(DateTimeFormatter.ISO_DATE_TIME), ticketDetails);
        }
    }

    public static class Response {
        @SerializedName("reserve_id")
        public String reserveId;
        public String price;

        public Response () {

        }

        public Response (String reserveId, String price) {
            this.reserveId = reserveId;
            this.price = price;
        }
    }

    public static class TicketDetails {
        @SerializedName("first_name")
        public String firstName;
        @SerializedName("last_name")
        public String lastName;
        public String email;
        public String section;

        @SerializedName("seat_number")
        public Integer seatNum = null;

        public TicketDetails () {

        }

        public TicketDetails (String section) {
            this(null, null, null, section);
        }

        public TicketDetails (String firstName, String lastName, String email, String section) {
            this.firstName = firstName;
            this.lastName = lastName;
            this.email = email;
            this.section = section;
        }

        public TicketDetails (String firstName, String lastName, String email, String section, Integer seatNum) {
            this.firstName = firstName;
            this.lastName = lastName;
            this.email = email;
            this.section = section;
            this.seatNum = seatNum;
        }
    }
}
