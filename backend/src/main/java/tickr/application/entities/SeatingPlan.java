package tickr.application.entities;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;

import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "seating_plan")
public class SeatingPlan {
    @Id
    @UuidGenerator
    @JdbcTypeCode(SqlTypes.CHAR)
    private UUID id;

    //@Column(name = "event_id")
    //private int eventId;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id")
    private Event event;

    //@Column(name = "location_id")
    //private int locationId;
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id")
    private Location location;

    public String section;

    @Column(name = "available_seats")
    public int availableSeats;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "section")
    private Set<Ticket> tickets;

    public SeatingPlan () {}    

    public SeatingPlan(Event event, Location location, String section, int availableSeats) {
        this.event = event;
        this.location = location;
        this.section = section;
        this.availableSeats = availableSeats;
    }

    private UUID getId () {
        return id;
    }

    private void setId (UUID id) {
        this.id = id;
    }

    private Event getEvent () {
        return event;
    }

    private void setEvent (Event event) {
        this.event = event;
    }

    private Location getLocation () {
        return location;
    }

    private void setLocation (Location location) {
        this.location = location;
    }

    private String getSection () {
        return section;
    }

    private void setSection (String section) {
        this.section = section;
    }

    private int getAvailableSeats () {
        return availableSeats;
    }

    private void setAvailableSeats (int availableSeats) {
        this.availableSeats = availableSeats;
    }

    private Set<Ticket> getTickets () {
        return tickets;
    }

    private void setTickets (Set<Ticket> tickets) {
        this.tickets = tickets;
    }
}
