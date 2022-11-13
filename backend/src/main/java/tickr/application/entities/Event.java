package tickr.application.entities;

import jakarta.persistence.*;
import org.hibernate.annotations.TimeZoneStorage;
import org.hibernate.annotations.TimeZoneStorageType;
import tickr.application.recommendations.EventVector;
import tickr.application.recommendations.SparseVector;
import tickr.application.serialised.SerializedLocation;
import tickr.application.serialised.requests.EditEventRequest;
import tickr.application.serialised.responses.EventViewResponse;
import tickr.application.serialised.responses.EventAttendeesResponse.Attendee;
import tickr.persistence.ModelSession;
import tickr.server.exceptions.BadRequestException;
import tickr.server.exceptions.ForbiddenException;
import tickr.util.EmailHelper;
import tickr.util.FileHelper;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;
import tickr.util.Utils;

import java.time.ZonedDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "termId.event", cascade = CascadeType.REMOVE)
    private Set<TfIdf> tfIdfs = new HashSet<>();

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "event", cascade = CascadeType.REMOVE)
    private Set<SeatingPlan> seatingPlans;

    @Column(name = "event_name")
    private String eventName;

    @TimeZoneStorage(TimeZoneStorageType.NORMALIZE_UTC)
    @Column(name = "event_start")
    private ZonedDateTime eventStart;

    @TimeZoneStorage(TimeZoneStorageType.NORMALIZE_UTC)
    @Column(name = "event_end")
    private ZonedDateTime eventEnd;

    @Column(name = "event_description")
    private String eventDescription;

    @Column(name = "seat_availability")
    private int seatAvailability = 0;

    @Column(name = "seat_capacity")
    private int seatCapacity = 0;

    @Column(name = "event_pic")
    private String eventPicture;

    private boolean published;

    @Column(name = "spotify_playlist")
    private String spotifyPlaylist;

    public Event() {}

    public Event(String eventName, User host, ZonedDateTime eventStart, ZonedDateTime eventEnd,
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

    public Event(String eventName, User host, ZonedDateTime eventStart, ZonedDateTime eventEnd,
            String eventDescription, Location location, int seatAvailability, String eventPicture, String spotifyPlaylist) {
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
        this.spotifyPlaylist = spotifyPlaylist;
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

    public Set<Ticket> getTickets () {
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

    public ZonedDateTime getEventStart () {
        return eventStart;
    }
    
    private void setEventStart (ZonedDateTime eventStart) {
        this.eventStart = eventStart;
    }

    public ZonedDateTime getEventEnd () {
        return eventEnd;
    }

    private void setEventEnd (ZonedDateTime eventEnd) {
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


    public Set<SeatingPlan> getSeatingPlans() {
        return seatingPlans;
    }

    public void setSeatingPlans(Set<SeatingPlan> seatingPlans) {
        this.seatingPlans = seatingPlans;
    }

    private boolean userHasTicket (User user) {
        return getTickets().stream()
                .anyMatch(t -> t.isOwnedBy(user));
    }

    private boolean userHasReview (User user) {
        return getComments().stream()
                .filter(Predicate.not(Comment::isReply))
                .anyMatch(c -> c.isWrittenBy(user));
    }

    private boolean userIsPrivileged (User user) {
        return getHost().getId().equals(user.getId())
                || getAdmins().stream().map(User::getId).anyMatch(id -> user.getId().equals(id));
    }

    public List<String> getUserTicketIds (User user) {
        List<String> set = new ArrayList<>();
        Set<Ticket> tmpTickets = this.tickets;
        List<Ticket> tickets = new ArrayList<>(tmpTickets);
        tickets.sort(new Comparator<>() {
            @Override
            public int compare (Ticket t1, Ticket t2) {
                if (t1.getSection().getSection().compareTo(t2.getSection().getSection()) == 0) {
                    Integer i1 = t1.getSeatNumber();
                    Integer i2 = t2.getSeatNumber();
                    return i1.compareTo(i2);
                }
                return t1.getSection().getSection().compareTo(t2.getSection().getSection());
            }
        });
        for (Ticket ticket : tickets) {
            if (ticket.getUser().equals(user)) {
                set.add(ticket.getId().toString());
            }
        }
        return set; 
    }

    public void editEvent (EditEventRequest request, ModelSession session, String eventName, String picture, SerializedLocation locations, String startDate, String endDate, String description, 
                             Set<String> categories, Set<String> tags, Set<String> admins, List<EditEventRequest.SeatingDetails> seatingDetails, boolean published, String spotifyPlaylist) {
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
            ZonedDateTime start_date;
            try {
                start_date = ZonedDateTime.parse(startDate, DateTimeFormatter.ISO_DATE_TIME);
                this.eventStart = start_date;
            } catch (DateTimeParseException e) {
                throw new ForbiddenException("Invalid date time string!", e);
            }
        }
        if (endDate != null) {
            ZonedDateTime end_date;
            try {
                end_date = ZonedDateTime.parse(endDate, DateTimeFormatter.ISO_DATE_TIME);
                this.eventEnd = end_date;
            } catch (DateTimeParseException e) {
                throw new ForbiddenException("Invalid date time string!", e);
            }
        }
        if (description != null) {
            this.eventDescription = description;
        }
        if (spotifyPlaylist != null) {
            this.spotifyPlaylist = spotifyPlaylist;
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
                    locations.suburb, locations.state, locations.country);
            session.save(newLocation);
            this.location = newLocation;
            this.location.lookupLongitudeLatitude();

            seatingPlans.forEach(s -> s.updateLocation(newLocation));
        }

        this.published = published;
    }

    public List<TicketReservation> makeReservations (ModelSession session, User user, ZonedDateTime requestedTime, String section,
                                                     int quantity, List<Integer> seatNums) {
        if (!published) {
            throw new ForbiddenException("Unable to reserve tickets from unpublished event!");
        }
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

    public List<Attendee> getAttendees (User user) {
        if (!canView(user)) {
            throw new ForbiddenException("Unable to view event!");
        }
        List<Ticket> tickets = new ArrayList<>(this.tickets); 
        if (tickets.size() == 0) {
            return new ArrayList<>();
        }
        Collections.sort(tickets, new Comparator<Ticket> () {
            @Override
            public int compare(Ticket t1, Ticket t2) {
                return t1.getUser().getId().toString().compareTo(t2.getUser().getId().toString());
            }
        });
        List<Attendee> attendees = new ArrayList<>();

        String prevUserId = tickets.get(0).getUser().getId().toString();
        Attendee attendee = new Attendee(prevUserId);
        for (Ticket ticket : tickets) {
            String currUserId = ticket.getUser().getId().toString();
            if (!currUserId.equals(prevUserId)) {
                prevUserId = currUserId;
                attendees.add(attendee);
                attendee = new Attendee(ticket.getUser().getId().toString());
                attendee.addTicketId(ticket.getId().toString());
            } else {
                attendee.addTicketId(ticket.getId().toString());
            }
        }
        attendees.add(attendee);
        return attendees;
    }
    
    public Comment addReview (ModelSession session, User author, String title, String text, float rating) {
        if (!userHasTicket(author)) {
            throw new ForbiddenException("You do not own a ticket for this event!");
        }

        if (userHasReview(author)) {
            throw new ForbiddenException("You have already made a review for this event!");
        }

        if (getEventStart().isAfter(ZonedDateTime.now(ZoneId.of("UTC")))) {
            throw new ForbiddenException("Cannot create review of event that hasn't happened!");
        }

        var comment = Comment.makeReview(this, author, title, text, rating);
        session.save(comment);
        getComments().add(comment);

        return comment;
    }

    public boolean canReply (User user) {
        return userIsPrivileged(user) || userHasTicket(user);
    }

    public void onDelete (ModelSession session) {
        if (eventPicture != null) {
            FileHelper.deleteFileAtUrl(eventPicture);
        }
    }

    public boolean matchesCategories (List<String> categories) {
        return categories.size() == 0 || getCategories().stream()
                .map(Category::getCategory)
                .anyMatch(categories::contains);
    }

    public boolean matchesTags (List<String> tags) {
        return tags.size() == 0 || getTags().stream()
                .map(Tag::getTags)
                .anyMatch(tags::contains);
    }

    public boolean startsAfter (ZonedDateTime startTime) {
        return startTime == null || getEventStart().isAfter(startTime) || getEventStart().isEqual(startTime);
    }

    public boolean endsBefore (ZonedDateTime endTime) {
        return endTime == null || getEventEnd().isBefore(endTime) || getEventEnd().isEqual(endTime);
    }

    public boolean matchesDescription (Set<String> words) {
        if (words.size() == 0) {
            return true;
        }
        var wordList = new HashSet<String>();
        wordList.addAll(Utils.toWords(getEventName()));
        wordList.addAll(Utils.toWords(getEventDescription()));

        return !Collections.disjoint(words, wordList);
    }

    public boolean canView (User user) {
        return published || (user != null && (getHost().getId().equals(user.getId()) ||
                getAdmins().stream().map(User::getId).anyMatch(Predicate.isEqual(user.getId()))));
    }

    public void makeAnnouncement (User user, String announcement) {
        if (announcement == null || announcement.equals("")) {
            throw new BadRequestException("Cannot make empty announcement!");
        }

        if (!userIsPrivileged(user)) {
            throw new ForbiddenException("You are not allowed to make an announcement!");
        }

        var seen = new HashSet<UUID>();

        for (var i : tickets) {
            if (!seen.contains(i.getUser().getId())) {
                EmailHelper.sendAnnouncement(user, i.getUser(), this, announcement);
                seen.add(i.getUser().getId());
            }
        }
    }

    public Map<String, Long> getWordCounts () {
        var nameMap = Utils.toWordsMap(eventName);
        var descMap = Utils.toWordsMap(eventDescription);

        return Stream.concat(nameMap.entrySet().stream(), descMap.entrySet().stream())
                .collect(Collectors.groupingBy(Map.Entry::getKey, Collectors.summingLong(Map.Entry::getValue)));
    }

    public SparseVector<String> getTfIdfVector (int numDocuments) {
        var keys = tfIdfs.stream().map(TfIdf::getTermString).collect(Collectors.toList());
        var values = tfIdfs.stream().map(t -> t.getTfIdf(numDocuments)).collect(Collectors.toList());

        return new SparseVector<>(keys, values).normalised();
    }

    public void setTfIdfs (List<TfIdf> tfIdfs) {
        this.tfIdfs.clear();
        this.tfIdfs.addAll(tfIdfs);
    }

    public SparseVector<String> getTagVector () {
        return new SparseVector<>(tags.stream().map(Tag::getTags).collect(Collectors.toList()), Collections.nCopies(tags.size(), 1.0))
                .normalised();
    }

    public SparseVector<String> getCategoryVector () {
        return new SparseVector<>(categories.stream().map(Category::getCategory).collect(Collectors.toList()), Collections.nCopies(categories.size(), 1.0))
                .normalised();
    }

    public double getDistance (Event other) {
        return getLocation().getDistance(other.getLocation());
    }

    public EventVector getEventVector (int numDocuments) {
        return new EventVector(getTfIdfVector(numDocuments), getTagVector(), getCategoryVector(),
                new SparseVector<>(List.of(host.getId().toString()), List.of(Utils.getIdf(host.getHostingEvents().size(), numDocuments))));
    }

    public EventViewResponse getEventViewResponse (SerializedLocation location, List<EventViewResponse.SeatingDetails> seatingResponse, Set<String> tags, Set<String> categories, Set<String> admins) {
        return new EventViewResponse(host.getId().toString(), eventName, eventPicture, location, eventStart.format(DateTimeFormatter.ISO_INSTANT), 
                eventEnd.format(DateTimeFormatter.ISO_INSTANT), eventDescription, seatingResponse,
                admins, categories, tags, published, seatAvailability, seatCapacity, spotifyPlaylist);
    }
}
