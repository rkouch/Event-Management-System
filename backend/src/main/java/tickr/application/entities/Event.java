package tickr.application.entities;

import jakarta.persistence.*;
import tickr.application.serialised.responses.EventViewResponse;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.HashSet;
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
    private Set<User> admins = new HashSet<>();

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "event")
    private Set<Ticket> tickets = new HashSet<>();

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "event")
    private Set<Category> categories = new HashSet<>();

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "event")
    private Set<Tag> tags = new HashSet<>();

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "event")
    private Set<Comment> comments;

    @Column(name = "event_name")
    private String eventName;

    @Column(name = "event_start")
    private LocalDateTime eventStart;

    @Column(name = "event_end")
    private LocalDateTime eventEnd;

    @Column(name = "event_description")
    private String eventDescription;

    @Column(name = "seat_availability")
    private int seatAvailability;

    @Column(name = "event_pic")
    private String eventPicture;

    public Event() {}

    public Event(String eventName, User host, LocalDateTime eventStart, LocalDateTime eventEnd,
            String eventDescription, Location location, int seatAvailability, String eventPicture) {
        this.location = location;
        this.eventName = eventName;
        this.eventStart = eventStart;
        this.eventEnd = eventEnd;
        this.eventDescription = eventDescription;
        this.seatAvailability = seatAvailability;
        this.host = host;
        this.eventPicture = eventPicture;
    }

    public UUID getId () {
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

    public Location getLocation () {
        return location;
    }

    public void setLocation (Location location) {
        this.location = location;
    }

    public Set<User> getAdmins () {
        return admins;
    }

    public void addAdmin (User admin) {
        this.admins.add(admin);
    }

    private Set<Ticket> getTickets () {
        return tickets;
    }

    private void setTickets (Set<Ticket> tickets) {
        this.tickets = tickets;
    }

    public Set<Category> getCategories () {
        return categories;
    }

    public void addCategory (Category category) {
        this.categories.add(category);
    }

    public Set<Tag> getTags () {
        return tags;
    }

    public void addTag (Tag tag) {
        this.tags.add(tag);
    }

    private Set<Comment> getComments () {
        return comments;
    }

    private void setComments (Set<Comment> comments) {
        this.comments = comments;
    }

    public String getEventName () {
        return eventName;
    }

    private void setEventName (String eventName) {
        this.eventName = eventName;
    }

    public LocalDateTime getEventStart () {
        return eventStart;
    }
    
    private void setEventStart (LocalDateTime eventStart) {
        this.eventStart = eventStart;
    }

    public LocalDateTime getEventEnd () {
        return eventEnd;
    }

    private void setEventEnd (LocalDateTime eventEnd) {
        this.eventEnd = eventEnd;
    }

    public String getEventDescription () {
        return eventDescription;
    }

    private void setEventDescription (String eventDescription) {
        this.eventDescription = eventDescription;
    }

    public int getSeatAvailability () {
        return seatAvailability;
    }

    private void setSeatAvailability (int seatAvailability) {
        this.seatAvailability = seatAvailability;
    }

    public void setAdmins(Set<User> admins) {
        this.admins = admins;
    }

    public void setCategories(Set<Category> categories) {
        this.categories = categories;
    }

    public void setTags(Set<Tag> tags) {
        this.tags = tags;
    }

    public String getEventPicture () {
        return eventPicture;
    }

    private void setEventPicture (String eventPicture) {
        this.eventPicture = eventPicture;
    }
}
