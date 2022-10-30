package tickr.application.entities;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;
import tickr.persistence.ModelSession;
import tickr.server.exceptions.ForbiddenException;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;
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

    @Column(name = "seat_availability")
    public int availableSeats;

    @Column(name = "total_seats")
    private int totalSeats = 0;

    @Column(name = "ticket_price")
    public float ticketPrice;

    @Column(name = "has_seats")
    public boolean hasSeats;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "section", cascade = CascadeType.REMOVE)
    private Set<TicketReservation> reservations;


    @OneToMany(fetch = FetchType.LAZY, mappedBy = "section", cascade = CascadeType.REMOVE)
    private Set<Ticket> tickets;

    public SeatingPlan () {}    

    public SeatingPlan(Event event, Location location, String section, int availableSeats, float ticketPrice, boolean hasSeats) {
        this.event = event;
        this.location = location;
        this.section = section;
        this.availableSeats = availableSeats;
        this.totalSeats = availableSeats;
        this.ticketPrice = ticketPrice;
        this.hasSeats = hasSeats;
    }

    private UUID getId () {
        return id;
    }

    private void setId (UUID id) {
        this.id = id;
    }

    public Event getEvent () {
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
        return totalSeats - tickets.size() - reservations.size();
    }

    public int getTotalSeats () {
        return totalSeats;
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

    

    public Set<TicketReservation> getReservations() {
        return reservations;
    }

    public void setReservations(Set<TicketReservation> reservations) {
        this.reservations = reservations;
    }

    private Set<Integer> getAllocatedNumbers () {
        var ticketSet = tickets.stream()
                .map(Ticket::getSeatNumber)
                .collect(Collectors.toSet());
        var reservedSet = reservations.stream()
                .filter(Predicate.not(TicketReservation::hasExpired))
                .map(TicketReservation::getSeatNum)
                .collect(Collectors.toSet());

        ticketSet.addAll(reservedSet);

        return ticketSet;
    }

    private List<TicketReservation> makeReservations (ModelSession session, User user, List<Integer> seatNums) {
        var newReservations = new ArrayList<TicketReservation>();
        for (var i : seatNums) {
            var reserve = new TicketReservation(user, this, i, ticketPrice);
            session.save(reserve);
            newReservations.add(reserve);
            reservations.add(reserve);
        }

        availableSeats -= seatNums.size();

        return newReservations;
    }

    public List<TicketReservation> reserveSeats (ModelSession session, User user, int quantity) {
        if (availableSeats < quantity) {
            throw new ForbiddenException("Not enough tickets remaining!");
        }

        var nums = getAllocatedNumbers().stream()
                .sorted()
                .collect(Collectors.toList());

        var reservedNums = new ArrayList<Integer>();

        int last = 0;
        int next = last;
        for (var i : nums) {
            if (reservedNums.size() == quantity) {
                break;
            }
            next = i;

            for (int j = last + 1; j < next; j++) {
                reservedNums.add(j);
                if (reservedNums.size() == quantity) {
                    break;
                }
            }

            last = i;
        }


        while (reservedNums.size() < quantity) {
            reservedNums.add(++next);
        }


        return makeReservations(session, user, reservedNums);
    }

    public List<TicketReservation> reserveSeats (ModelSession session, User user, List<Integer> seatNums) {
        var takenNums = getAllocatedNumbers();
        if (seatNums.stream().anyMatch(i -> i <= 0 || i > totalSeats || takenNums.contains(i))) {
            throw new ForbiddenException("One or more ticket number is already taken!");
        }

        return makeReservations(session, user, seatNums);
    }
}
