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
@Table(name = "invitation")
public class Invitation {
    @Id
    @UuidGenerator
    @JdbcTypeCode(SqlTypes.CHAR)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id")
    private Group group;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reserve_id")
    private TicketReservation ticketReservation;


    public Invitation(Group group, TicketReservation ticketReservation) {
        this.group = group;
        this.ticketReservation = ticketReservation;
    }

    public Invitation() {}

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Group getGroup() {
        return group;
    }

    public void setGroup(Group group) {
        this.group = group;
    }

    public TicketReservation getTicketReservation() {
        return ticketReservation;
    }

    public void setTicketReservation(TicketReservation ticketReservation) {
        this.ticketReservation = ticketReservation;
    }

    public void acceptInvitation(User user) {
        group.acceptInvitation(this, user);
        ticketReservation.acceptInvitation(user);
        user.addReservation(ticketReservation);
    }

    public void denyInvitation() {
        group.removeInvitation(this);
        ticketReservation.setInvitation(null);
        ticketReservation.setGroupAccepted(false);
    }
}
