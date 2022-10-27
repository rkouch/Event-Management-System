package tickr.application.entities;

import jakarta.persistence.*;
import tickr.application.serialised.SerializedLocation;
import tickr.application.serialised.combined.TicketReserve;
import tickr.application.serialised.requests.EditEventRequest;
import tickr.application.serialised.requests.CreateEventRequest.SeatingDetails;
import tickr.application.serialised.responses.EventViewResponse;
import tickr.application.serialised.responses.TicketViewResponse;
import tickr.persistence.ModelSession;
import tickr.server.exceptions.BadRequestException;
import tickr.server.exceptions.ForbiddenException;
import tickr.util.FileHelper;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Entity
@Table(name = "events")
public class Event {
    static final Logger logger = LogManager.getLogger();

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
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    @JoinColumn(name = "location_id")
    private Location location;

    //@OneToMany(fetch = FetchType.LAZY, mappedBy = "event")
    //private Set<EventAdmin> admins;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "admins",
            joinColumns = {@JoinColumn(name = "event_id")},
            inverseJoinColumns = {@JoinColumn(name = "user_id")})
    private Set<User> admins = new HashSet<>();

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "event", cascade = CascadeType.REMOVE)
    private Set<Ticket> tickets = new HashSet<>();

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "event", cascade = CascadeType.REMOVE)
    private Set<Category> categories = new HashSet<>();

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "event", cascade = CascadeType.REMOVE)
    private Set<Tag> tags = new HashSet<>();

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "event", cascade = CascadeType.REMOVE)
    private Set<Comment> comments;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "event", cascade = CascadeType.REMOVE)
    private Set<SeatingPlan> seatingPlans;

    @Column(name = "event_name")
    private String eventName;

    @Column(name = "event_start")
    private LocalDateTime eventStart;

    @Column(name = "event_end")
    private LocalDateTime eventEnd;

    @Column(name = "event_description")
    private String eventDescription;

    @Column(name = "seat_availability")
    private int seatAvailability = 0;

    @Column(name = "seat_capacity")
    private int seatCapacity = 0;

    @Column(name = "event_pic")
    private String eventPicture;

    private boolean published;

    public Event() {}

    public Event(String eventName, User host, LocalDateTime eventStart, LocalDateTime eventEnd,
            String eventDescription, Location location, int seatAvailability, String eventPicture) {
        this.location = location;
        this.eventName = eventName;
        this.eventStart = eventStart;
        this.eventEnd = eventEnd;
        this.eventDescription = eventDescription;
        this.seatAvailability = seatAvailability;
        this.seatCapacity = seatAvailability;
        this.host = host;
        this.eventPicture = eventPicture;
        this.published = false;
    }

    public UUID getId () {
        return id;
    }

    private void setId (UUID id) {
        this.id = id;
    }

    public User getHost () {
        return host;
    }

    public void setHost (User host) {
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

    private void clearAdmins () {
        this.admins.clear();
    }

    public boolean isPublished() {
        return published;
    }

    public void setPublished(boolean published) {
        this.published = published;
    }

    public int getSeatCapacity() {
        return seatCapacity;
    }

    public void setSeatCapacity(int seatCapacity) {
        this.seatCapacity = seatCapacity;
    }

    public List<String> getUserTicketIds (User user) {
        List<String> set = new ArrayList<>();
        Set<Ticket> tmpTickets = this.tickets;
        List<Ticket> tickets = new ArrayList<>(tmpTickets);
        Collections.sort(tickets, new Comparator<Ticket>() {
            @Override
            public int compare(Ticket t1, Ticket t2) {
                if (t1.getSection().getSection().compareTo(t2.getSection().getSection()) == 0) {
                    Integer i1 = t1.getSeatNumber();
                    Integer i2 = t2.getSeatNumber();
                    return i1.compareTo(i2);
                }
                return t1.getSection().getSection().compareTo(t2.getSection().getSection());
            }
        });
        for (Ticket ticket : tickets) {
            if (ticket.getUser() == user) {
                set.add(ticket.getId().toString());
            }
        }
        return set; 
    }

    public void editEvent (EditEventRequest request, ModelSession session, String eventName, String picture, SerializedLocation locations, String startDate, String endDate, String description, 
                             Set<String> categories, Set<String> tags, Set<String> admins, List<EditEventRequest.SeatingDetails> seatingDetails, boolean published) {
        if (eventName != null) {
            this.eventName = eventName; 
        }
        if (picture != null) {
            if (!getEventPicture().equals("")) {
                FileHelper.deleteFileAtUrl(getEventPicture());
            }
            this.eventPicture = picture;
        }
        if (startDate != null) {
            LocalDateTime start_date;
            try {
                start_date = LocalDateTime.parse(startDate, DateTimeFormatter.ISO_DATE_TIME);
                this.eventStart = start_date;
            } catch (DateTimeParseException e) {
                throw new ForbiddenException("Invalid date time string!");
            }
        }
        if (endDate != null) {
            LocalDateTime end_date;
            try {
                end_date = LocalDateTime.parse(endDate, DateTimeFormatter.ISO_DATE_TIME);
                this.eventEnd = end_date;
            } catch (DateTimeParseException e) {
                throw new ForbiddenException("Invalid date time string!");
            }
        }
        if (description != null) {
            this.eventDescription = description;
        }
        if (categories != null) {
            List<Category> oldCat = session.getAllWith(Category.class, "event", this);
            for (Category cat : oldCat) {
                session.remove(cat);
            }
            this.categories.clear();
            for (String cat : categories) {
                Category newCat = new Category(cat);
                newCat.setEvent(this);
                session.save(newCat);
                this.addCategory(newCat); 
            }
        }
        if (tags != null) {
            List<Tag> oldTags = session.getAllWith(Tag.class, "event", this);
            for (Tag tag : oldTags) {
                session.remove(tag);
            }
            this.tags.clear();
            for (String tag : tags) {
                Tag newTag = new Tag(tag);
                newTag.setEvent(this);
                session.save(newTag);
                this.addTag(newTag); 
            }
        }
        if (admins != null) {
            this.clearAdmins();

            for (String admin : admins) {
                User userAdmin;
                try {
                    userAdmin = session.getById(User.class, UUID.fromString(admin))
                    .orElseThrow(() -> new ForbiddenException(String.format("Unknown account \"%s\".", admin)));
                } catch (IllegalArgumentException e) {
                    throw new ForbiddenException("invalid admin Id");
                }
                this.addAdmin(userAdmin);
                userAdmin.addAdminEvents(this);
            }
        }
        if (seatAvailability == seatCapacity) {
            if (seatingDetails != null) {
                // error here
                for (SeatingPlan seat : seatingPlans) {
                    session.remove(seat);
                }
                //
                seatingPlans.clear();
                for (EditEventRequest.SeatingDetails seats : seatingDetails) {
                    SeatingPlan seatingPlan = new SeatingPlan(this, this.location, seats.section, seats.availability, seats.ticketPrice, seats.hasSeats);
                    session.save(seatingPlan);
                    seatingPlans.add(seatingPlan);
                }
                this.seatAvailability = request.getSeatCapacity();
                this.seatCapacity = request.getSeatCapacity();
            }
        } else {
            throw new BadRequestException("Cannot edit seating details for an event where tickets have been reserved/purchased!");
        }

        if (locations != null) {
            session.remove(this.location);
            Location newLocation = new Location(locations.streetNo, locations.streetName, locations.unitNo, locations.postcode,
                    locations.suburb, locations.state, locations.country, locations.longitude, locations.latitude);
            session.save(newLocation);
            this.location = newLocation;

            seatingPlans.forEach(s -> s.updateLocation(newLocation));
        }

        this.published = published;
    }

    public List<TicketReservation> makeReservations (ModelSession session, User user, LocalDateTime requestedTime, String section,
                                                     int quantity, List<Integer> seatNums) {
        if (section == null) {
            throw new BadRequestException("Null section!");
        } else if (quantity <= 0 || (seatNums.size() != 0 && seatNums.size() != quantity)) {
            throw new BadRequestException("Invalid quantity of seats!");
        } else if (eventStart.isAfter(requestedTime) || eventEnd.isBefore(requestedTime)) {
            throw new ForbiddenException("Invalid requested time!");
        }

        for (var i : seatingPlans) {
            if (i.getSection().equals(section)) {
                return seatNums.size() != 0 ? i.reserveSeats(session, user, seatNums) : i.reserveSeats(session, user, quantity);
            }
        }

        throw new ForbiddenException("Invalid section!");
    }
}
