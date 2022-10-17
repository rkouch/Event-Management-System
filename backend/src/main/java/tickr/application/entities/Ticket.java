package tickr.application.entities;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;

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

    private UUID getId () {
        return id;
    }

    private void setId (UUID id) {
        this.id = id;
    }

    private User getUser () {
        return user;
    }

    private void setUser (User user) {
        this.user = user;
    }

    private Event getEvent () {
        return event;
    }

    private void setEvent (Event event) {
        this.event = event;
    }

    private SeatingPlan getSection () {
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
}
