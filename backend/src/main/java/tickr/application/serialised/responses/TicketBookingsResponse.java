package tickr.application.serialised.responses;

import java.util.HashSet;
import java.util.Set;

public class TicketBookingsResponse {
    public Set<String> tickets = new HashSet<>();

    public TicketBookingsResponse(Set<String> tickets) {
        this.tickets = tickets;
    } 

}
