package tickr.application.entities;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;
import tickr.persistence.ModelSession;
import tickr.server.exceptions.ForbiddenException;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

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

    private String section;

    @Column(name = "available_seats")
    public int availableSeats;

    @Column(name = "total_seats")
    private int totalSeats = 0;

    @Column(name = "ticket_price")
    public int ticketPrice;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "section")
    private Set<TicketReservation> reservations;


    @OneToMany(fetch = FetchType.LAZY, mappedBy = "section")
    private Set<Ticket> tickets;

    public SeatingPlan () {}    

    public SeatingPlan(Event event, Location location, String section, int availableSeats, int ticketPrice) {
        this.event = event;
        this.location = location;
        this.section = section;
        this.availableSeats = availableSeats;
        this.totalSeats = availableSeats;
        this.ticketPrice = ticketPrice;
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

    public String getSection () {
        return section;
    }

    private void setSection (String section) {
        this.section = section;
    }

    public int getAvailableSeats () {
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

    public void updateLocation (Location location) {
        setLocation(location);
    }

    private Set<Integer> getAllocatedNumbers () {
        var ticketSet = tickets.stream().map(Ticket::getSeatNumber).collect(Collectors.toSet());
        var reservedSet = reservations.stream().map(TicketReservation::getSeatNum).collect(Collectors.toSet());

        ticketSet.addAll(reservedSet);

        return ticketSet;
    }

    public TicketReservation reserveSeat (ModelSession session, User user, EventReservation eventReservation, String firstName, String lastName, String email) {
        if (availableSeats == 0) {
            throw new ForbiddenException("No seats remaining!");
        }
        var ticketNum = getAllocatedNumbers().stream()
                .max(Integer::compareTo)
                .orElse(0) + 1;

        var reservation = new TicketReservation(user, this, ticketNum, eventReservation, ticketPrice, firstName, lastName, email);
        session.save(reservation);

        reservations.add(reservation);

        availableSeats--;

        return reservation;
    }

    public TicketReservation reserveSeat (ModelSession session, User user, int seatNum, EventReservation eventReservation, String firstName,
                                          String lastName, String email) {
        if (availableSeats == 0 || seatNum <= 0 || seatNum >= totalSeats || getAllocatedNumbers().contains(seatNum)) {
            throw new ForbiddenException("Seat number is invalid!");
        }

        var reservation = new TicketReservation(user, this, seatNum, eventReservation, ticketPrice, firstName, lastName, email);
        session.save(reservation);

        reservations.add(reservation);

        availableSeats--;

        return reservation;
    }
}
