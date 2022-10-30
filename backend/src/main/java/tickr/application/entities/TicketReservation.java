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

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;

@Entity
@Table(name = "ticket_reservation")
public class TicketReservation {

    private static final Duration EXPIRY_DURATION = Duration.ofMinutes(5);
    @Id
    @UuidGenerator
    @JdbcTypeCode(SqlTypes.CHAR)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seating_id")
    private SeatingPlan section;

    private float price;

    @Column(name = "seat_num")
    private int seatNum;

    @Column(name = "expiry_time")
    private LocalDateTime expiryTime;

    @OneToOne(fetch = FetchType.LAZY, mappedBy = "ticketReservation", cascade = CascadeType.REMOVE)
    private PurchaseItem purchaseItem;

    public TicketReservation () {

    }

    public TicketReservation (User user, SeatingPlan section, int seatNum, float price) {
        this.user = user;
        this.section = section;
        this.seatNum = seatNum;
        this.price = price;
        this.expiryTime = LocalDateTime.now(ZoneId.of("UTC"))
                .plus(EXPIRY_DURATION);
    }

    public int getSeatNum () {
        return seatNum;
    }

    public float getPrice () {
        return price;
    }

    public void setExpiry (LocalDateTime expiryTime) {
        this.expiryTime = expiryTime;
    }

    public boolean hasExpired () {
        return LocalDateTime.now(ZoneId.of("UTC"))
                .isAfter(expiryTime);
    }

    public TicketReserve.ReserveDetails getDetails () {
        return new TicketReserve.ReserveDetails(id.toString(), seatNum, section.getSection(), price);
    }

    public IOrderBuilder registerPurchaseItem (ModelSession session, IOrderBuilder builder, UUID purchaseId, User user,
                                               String firstName, String lastName, String email) {
        if (!this.user.getId().equals(user.getId())) {
            throw new ForbiddenException("User is forbidden from purchasing this reservation!");
        }

        if (hasExpired()) {
            throw new ForbiddenException("Ticket reservation has expired!");
        }

        var purchaseItem = new PurchaseItem(purchaseId, this, firstName, lastName, email);
        session.save(purchaseItem);

        return builder.withLineItem(new LineItem(String.format("%s (%s%d)", getSection().getEvent().getEventName(), getSection().getSection().substring(0, 1), getSeatNum()), this.price));
    }

    public Ticket convert (String firstName, String lastName, String email) {
        return new Ticket(user, section, seatNum, firstName, lastName, email);
    }

    public SeatingPlan getSection() {
        return section;
    }

    public void setSection(SeatingPlan section) {
        this.section = section;
    }

    public boolean canCancel (User user) {
        return this.user.getId().equals(user.getId()) && purchaseItem == null;
    }
    
}
