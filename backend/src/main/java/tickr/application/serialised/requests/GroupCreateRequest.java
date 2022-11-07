package tickr.application.serialised.requests;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.google.gson.annotations.SerializedName;

import tickr.application.entities.TicketReservation;
import tickr.persistence.ModelSession;
import tickr.server.exceptions.ForbiddenException;

public class GroupCreateRequest {
    @SerializedName("auth_token")
    public String authToken;

    @SerializedName("reserved_ids")
    public List<String> reservedIds;

    public GroupCreateRequest(String authToken, List<String> reservedIds) {
        this.authToken = authToken;
        this.reservedIds = reservedIds;
    } 

    public Set<TicketReservation> getTicketReservations (ModelSession session) {
        Set<TicketReservation> set = new HashSet<>();
        for (String id : reservedIds) {
            var reserveId = session.getById(TicketReservation.class, UUID.fromString(id))
                    .orElseThrow(() -> new ForbiddenException("Reserve ID does not exist!"));
            set.add(reserveId);
        }
        return set;
    }
}
