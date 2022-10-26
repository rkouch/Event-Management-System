package tickr.application.serialised.responses;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TicketBookingsResponse {
    public List<String> tickets = new ArrayList<>();

    public TicketBookingsResponse(List<String> tickets) {
        this.tickets = tickets;
    } 

}
