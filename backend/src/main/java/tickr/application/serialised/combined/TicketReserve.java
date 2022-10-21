package tickr.application.serialised.combined;

import com.google.gson.annotations.SerializedName;
import tickr.server.exceptions.BadRequestException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
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
            this.eventId = eventId;
            this.ticketDateTime = ticketDateTime;
            this.ticketDetails = ticketDetails;
        }

        public Request (String authToken, String eventId, LocalDateTime ticketDateTime, List<TicketDetails> ticketDetails) {
            this(authToken, eventId, ticketDateTime.format(DateTimeFormatter.ISO_DATE_TIME), ticketDetails);
        }

        public LocalDateTime getTicketTime () {
            try {
                return LocalDateTime.parse(ticketDateTime, DateTimeFormatter.ISO_DATE_TIME);
            } catch (DateTimeParseException e) {
                throw new BadRequestException("Invalid datetime!", e);
            }
        }
    }

    public static class RequestNew {
        @SerializedName("auth_token")
        public String authToken = null;

        @SerializedName("event_id")
        public String eventId = null;

        @SerializedName("ticket_datetime")
        public String ticketDateTime = null;

        @SerializedName("ticket_details")
        public List<TicketDetailsNew> ticketDetails = null;

        public RequestNew () {

        }

        public RequestNew (String authToken, String eventId, String ticketDateTime, List<TicketDetailsNew> ticketDetails) {
            this.authToken = authToken;
            this.eventId = eventId;
            this.ticketDateTime = ticketDateTime;
            this.ticketDetails = ticketDetails;
        }

        public RequestNew (String authToken, String eventId, LocalDateTime ticketDateTime, List<TicketDetailsNew> ticketDetails) {
            this(authToken, eventId, ticketDateTime.format(DateTimeFormatter.ISO_DATE_TIME), ticketDetails);
        }

        public LocalDateTime getTicketTime () {
            try {
                return LocalDateTime.parse(ticketDateTime, DateTimeFormatter.ISO_DATE_TIME);
            } catch (DateTimeParseException e) {
                throw new BadRequestException("Invalid datetime!", e);
            }
        }
    }

    public static class ResponseNew {
        @SerializedName("reserve_tickets")
        public List<ReserveDetails> reserveTickets;

        public ResponseNew () {

        }

        public ResponseNew (List<ReserveDetails> reserveTickets) {
            this.reserveTickets = reserveTickets;
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

    public static class TicketDetailsNew {
        public String section;
        public int quantity;
        @SerializedName("seat_numbers")
        public List<Integer> seatNums;


        public TicketDetailsNew () {

        }

        public TicketDetailsNew (String section, int quantity, List<Integer> seatNums) {
            this.section = section;
            this.quantity = quantity;
            this.seatNums = seatNums;
        }
    }

    public static class ReserveDetails {
        @SerializedName("reserve_id")
        public String reserveId;

        @SerializedName("seat_number")
        public int seatNum;
        public String section;
        public float price;

        public ReserveDetails () {

        }

        public ReserveDetails (String reserveId, int seatNum, String section, float price) {
            this.reserveId = reserveId;
            this.seatNum = seatNum;
            this.section = section;
            this.price = price;
        }
    }
}
