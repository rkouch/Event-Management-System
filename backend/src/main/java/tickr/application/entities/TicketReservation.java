package tickr.application.entities;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;
import tickr.application.apis.purchase.IOrderBuilder;
import tickr.application.apis.purchase.LineItem;
import tickr.application.serialised.combined.TicketReserve;
import tickr.persistence.ModelSession;
import tickr.server.exceptions.ForbiddenException;

import java.util.UUID;

@Entity
@Table(name = "ticket_reservation")
public class TicketReservation {
    @Id
    @UuidGenerator
    @JdbcTypeCode(SqlTypes.CHAR)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    /*@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id")
    private EventReservation eventReservation;*/

    /*@Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "email")
    private String email;*/

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seating_id")
    private SeatingPlan section;

    private float price;

    @Column(name = "seat_num")
    private int seatNum;

    public TicketReservation () {

    }

    public TicketReservation (User user, SeatingPlan section, int seatNum, EventReservation eventReservation, float price, String firstName, String lastName, String email) {
        this.user = user;
        this.section = section;
        this.seatNum = seatNum;
        //this.firstName = firstName;
        //this.lastName = lastName;
        //this.email = email;
        //this.eventReservation = eventReservation;
        this.price = price;
    }

    public TicketReservation (User user, SeatingPlan section, int seatNum, float price) {
        this.user = user;
        this.section = section;
        this.seatNum = seatNum;
        //this.firstName = firstName;
        //this.lastName = lastName;
        //this.email = email;
        //this.eventReservation = eventReservation;
        this.price = price;
    }

    public int getSeatNum () {
        return seatNum;
    }

    public float getPrice () {
        return price;
    }

    public LineItem makeLineItem () {
        return new LineItem(this.section.getSection(), this.price);
    }

    public Ticket convert (Event event) {
        return new Ticket(user, event, section, seatNum);
    }

    public TicketReserve.ReserveDetails getDetails () {
        return new TicketReserve.ReserveDetails(id.toString(), seatNum, section.getSection(), price);
    }

    public IOrderBuilder registerPurchaseItem (ModelSession session, IOrderBuilder builder, UUID purchaseId, User user,
                                               String firstName, String lastName, String email) {
        if (!this.user.getId().equals(user.getId())) {
            throw new ForbiddenException("User is forbidden from purchasing this reservation!");
        }

        var purchaseItem = new PurchaseItem(purchaseId, this, firstName, lastName, email);
        session.save(purchaseItem);

        return builder.withLineItem(new LineItem(this.section.getSection() + " " + this.seatNum, this.price));
    }

    public Ticket convert (String firstName, String lastName, String email) {
        return new Ticket(user, section, seatNum);
    }
}
