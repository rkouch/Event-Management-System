package tickr.application.entities;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "event_reservation")
public class EventReservation {
    @Id
    @UuidGenerator
    @JdbcTypeCode(SqlTypes.CHAR)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id")
    private Event event;

    private float price;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "eventReservation")
    private Set<TicketReservation> ticketReservations;

    public EventReservation () {

    }

    public EventReservation (User user, Event event) {
        this.user = user;
        this.event = event;
        this.ticketReservations = new HashSet<>();
        this.price = 0.0f;
    }

    public UUID getId () {
        return id;
    }

    public void addTicketReservation (TicketReservation reservation) {
        ticketReservations.add(reservation);
        price += reservation.getPrice();
    }

    public float getPrice () {
        return price;
    }
}
