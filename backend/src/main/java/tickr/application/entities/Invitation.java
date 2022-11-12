package tickr.application.entities;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;
import tickr.application.apis.purchase.IOrderBuilder;
import tickr.application.apis.purchase.LineItem;
import tickr.application.serialised.combined.TicketReserve;
import tickr.application.serialised.responses.GroupDetailsResponse.PendingInvite;
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    public Invitation(Group group, TicketReservation ticketReservation, User user) {
        this.group = group;
        this.ticketReservation = ticketReservation;
        this.user = user;
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
    }

    public void denyInvitation() {
        group.removeInvitation(this);
        user.removeInvitation(this);
        ticketReservation.denyInvitation();
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void handleInvitation(Group group, TicketReservation reserve, User user) {
        group.addInvitation(this);
        reserve.setInvitation(this);
        user.addInvitation(this);
    }

    public PendingInvite createPendingInviteDetails() {
        return new PendingInvite(user.getEmail(), ticketReservation.getSection().getSection(), ticketReservation.getSeatNum(), id.toString());
    }
}
