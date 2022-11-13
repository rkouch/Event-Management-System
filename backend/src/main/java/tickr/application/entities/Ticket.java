package tickr.application.entities;

import jakarta.persistence.*;
import tickr.application.apis.ApiLocator;
import tickr.application.apis.purchase.IPurchaseAPI;
import tickr.application.serialised.responses.TicketViewResponse;
import tickr.application.serialised.responses.GroupDetailsResponse.GroupMember;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;
import tickr.server.exceptions.ForbiddenException;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "tickets")
public class Ticket {
    @Id
    @UuidGenerator
    @JdbcTypeCode(SqlTypes.CHAR)
    private UUID id;

    //@Column(name = "user_id")
    //private int userId;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    //@Column(name = "event_id")
    //private int eventId;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id")
    private Event event;

    //@Column(name = "section_id")
    //private int sectionId;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "section_id")
    private SeatingPlan section;

    @Column(name = "seat_no")
    private int seatNumber;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    private String email;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id")
    private Group group;

    @Column(name = "payment_id")
    private String paymentId;

    private long price;

    public UUID getId () {
        return id;
    }

    private void setId (UUID id) {
        this.id = id;
    }

    public User getUser () {
        return user;
    }

    private void setUser (User user) {
        this.user = user;
    }

    public Event getEvent () {
        return event;
    }

    private void setEvent (Event event) {
        this.event = event;
    }

    public SeatingPlan getSection () {
        return section;
    }

    private void setSection (SeatingPlan section) {
        this.section = section;
    }

    public int getSeatNumber () {
        return seatNumber;
    }

    private void setSeatNumber (int seatNumber) {
        this.seatNumber = seatNumber;
    }

    public String getFirstName () {
        return firstName != null ? firstName : user.getFirstName();
    }

    public String getLastName () {
        return lastName != null ? lastName : user.getLastName();
    }

    public String getEmail () {
        return email != null ? email : user.getEmail();
    }

    public Ticket () {

    }
    /*public Ticket (User user, SeatingPlan section, int seatNumber) {
        this(user, section, seatNumber, null, null, null);
    }*/

    public Ticket (User user,SeatingPlan section, int seatNumber, String firstName, String lastName, String email, String paymentId, long price) {
        this.user = user;
        this.event = section.getEvent();
        this.section = section;
        this.seatNumber = seatNumber;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;

        this.paymentId = paymentId;
        this.price = price;
    }

    public TicketViewResponse getTicketViewResponse () {
        return group == null || !group.getLeader().equals(this.getUser())
        ? new TicketViewResponse(this.event.getId().toString(), this.user.getId().toString(), this.section.getSection(), this.seatNumber,
                this.firstName, this.lastName, this.email, null) 
        : new TicketViewResponse(this.event.getId().toString(), this.user.getId().toString(), this.section.getSection(), this.seatNumber,
                this.firstName, this.lastName, this.email, group.getId().toString());
    }

    public boolean isOwnedBy (User user) {
        return user != null && this.user.getId().equals(user.getId());
    }

    public Group getGroup() {
        return group;
    }

    public void setGroup(Group group) {
        this.group = group;
    }

    // public Users createUsersDetails() {
    //     return new Users(user.getId().toString(), email, section.getSection(), seatNumber, true);
    // }

    public GroupMember createGroupMemberDetails() {
        return new GroupMember(user.getEmail(), section.getSection(), seatNumber, true);
    }

    public void refund (User user) {
        if (!isOwnedBy(user)) {
            throw new ForbiddenException("Attempted to refund other users ticket!");
        } else if (getEvent().getEventStart().isBefore(ZonedDateTime.now())) {
            throw new ForbiddenException("Attempted to refund ticket after event has started!");
        }

        if (paymentId != null) {
            ApiLocator.locateApi(IPurchaseAPI.class).refundItem(paymentId, price);
        }
    }
}
