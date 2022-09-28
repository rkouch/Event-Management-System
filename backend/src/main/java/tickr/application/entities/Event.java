package tickr.application.entities;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "events")
public class Event {
    @Id
    @UuidGenerator
    @JdbcTypeCode(SqlTypes.CHAR)
    private UUID id;

    //@Column(name = "host_id")
    //private int hostId;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "host_id")
    private User host;

    //@Column(name = "location_id")
    //private int locationId;
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id")
    private Location location;

    //@OneToMany(fetch = FetchType.LAZY, mappedBy = "event")
    //private Set<EventAdmin> admins;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "admins",
            joinColumns = {@JoinColumn(name = "event_id")},
            inverseJoinColumns = {@JoinColumn(name = "user_id")})
    private Set<User> admins;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "event")
    private Set<Ticket> tickets;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "event")
    private Set<Category> categories;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "event")
    private Set<Tag> tags;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "event")
    private Set<Comment> comments;

    @Column(name = "event_name")
    private String eventName;

    @Column(name = "event_date")
    private LocalDateTime eventDate;

    @Column(name = "event_description")
    private String eventDescription;

    @Column(name = "seat_availability")
    private int seatAvailability;

    private UUID getId () {
        return id;
    }

    private void setId (UUID id) {
        this.id = id;
    }

    private User getHost () {
        return host;
    }

    private void setHost (User host) {
        this.host = host;
    }

    private Location getLocation () {
        return location;
    }

    private void setLocation (Location location) {
        this.location = location;
    }

    private Set<User> getAdmins () {
        return admins;
    }

    private void setAdmins (Set<User> admins) {
        this.admins = admins;
    }

    private Set<Ticket> getTickets () {
        return tickets;
    }

    private void setTickets (Set<Ticket> tickets) {
        this.tickets = tickets;
    }

    private Set<Category> getCategories () {
        return categories;
    }

    private void setCategories (Set<Category> categories) {
        this.categories = categories;
    }

    private Set<Tag> getTags () {
        return tags;
    }

    private void setTags (Set<Tag> tags) {
        this.tags = tags;
    }

    private Set<Comment> getComments () {
        return comments;
    }

    private void setComments (Set<Comment> comments) {
        this.comments = comments;
    }

    private String getEventName () {
        return eventName;
    }

    private void setEventName (String eventName) {
        this.eventName = eventName;
    }

    private LocalDateTime getEventDate () {
        return eventDate;
    }

    private void setEventDate (LocalDateTime eventDate) {
        this.eventDate = eventDate;
    }

    private String getEventDescription () {
        return eventDescription;
    }

    private void setEventDescription (String eventDescription) {
        this.eventDescription = eventDescription;
    }

    private int getSeatAvailability () {
        return seatAvailability;
    }

    private void setSeatAvailability (int seatAvailability) {
        this.seatAvailability = seatAvailability;
    }
}
