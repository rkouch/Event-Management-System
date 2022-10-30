package tickr.application.entities;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;
import tickr.application.apis.ApiLocator;
import tickr.application.apis.email.IEmailAPI;
import tickr.application.serialised.combined.NotificationManagement;
import tickr.application.serialised.responses.ViewProfileResponse;
import tickr.persistence.ModelSession;
import tickr.server.exceptions.BadRequestException;
import tickr.server.exceptions.ForbiddenException;
import tickr.util.CryptoHelper;
import tickr.util.FileHelper;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

@Entity
@Table(name = "users", uniqueConstraints = @UniqueConstraint(columnNames = {"email"}))
public class User {
    @Id
    @UuidGenerator
    @JdbcTypeCode(SqlTypes.CHAR)
    private UUID id;

    @Column(name = "email", unique = true)
    private String email;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "password_hash")
    private char[] passwordHash;
    private String username;
    private LocalDate dob;

    private boolean reminders = true;

    @Column(name = "is_host")
    private boolean isHost;

    private String description;

    @Column(name = "profile_pic")
    private String profilePicture;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "user", cascade = CascadeType.ALL)
    private Set<AuthToken> tokens = new HashSet<>();

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "user", cascade = CascadeType.REMOVE)
    private Set<ResetToken> resetTokens = new HashSet<>();

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "host", cascade = CascadeType.ALL)
    private Set<Event> hostingEvents = new HashSet<>();

    //@OneToMany(fetch = FetchType.LAZY, mappedBy = "user")
    //private Set<EventAdmin> adminEvents;

    @ManyToMany(fetch = FetchType.LAZY, mappedBy = "admins", cascade = {})
    private Set<Event> adminEvents = new HashSet<>();

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "leader", cascade = CascadeType.ALL)
    private Set<Group> ownedGroups = new HashSet<>();

    @ManyToMany(fetch = FetchType.LAZY, mappedBy = "users", cascade = {})
    private Set<Group> groups = new HashSet<>();

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "user", cascade = CascadeType.ALL)
    private Set<Ticket> tickets = new HashSet<>();

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "author")
    private Set<Comment> comments = new HashSet<>();

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "author")
    private Set<Reaction> reactions = new HashSet<>();

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "user", cascade = CascadeType.REMOVE)
    private Set<TicketReservation> reservations;

    public User () {

    }

    public User (String email, String password, String username, String firstName, String lastName, LocalDate dob) {
        this.email = email;
        this.passwordHash = CryptoHelper.hashPassword(password);
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.dob = dob;
        this.isHost = false;

        this.description = "";
        this.profilePicture = "";
    }

    /**
     * Makes an auth token for the user
     * @param session
     * @param expiryDuration the requested expiry duration for the token
     * @return the resulting auth token
     */
    public AuthToken makeToken (ModelSession session, Duration expiryDuration) {
        var token = new AuthToken(this, LocalDateTime.now(ZoneId.of("UTC")), expiryDuration);
        session.save(token);
        getTokens().add(token);

        return token;
    }

    public AuthToken authenticatePassword (ModelSession session, String password, Duration tokenExpiryDuration) {
        if (CryptoHelper.verifyHash(password, getPasswordHash())) {
            return makeToken(session, tokenExpiryDuration);
        } else {
            throw new ForbiddenException("Incorrect password.");
        }
    }

    /**
     * Checks that the password matches the stored password hash
     * @param password
     * @return true if valid, false if not
     */
    public boolean verifyPassword (String password) {
        return CryptoHelper.verifyHash(password, getPasswordHash());
    }

    /**
     * Changes password hash to another password hash
     * @param newPassword
     * @return
     */
    public void changePassword (ModelSession session, String newPassword) {
        this.passwordHash = CryptoHelper.hashPassword(newPassword);
        
        for (var i : tokens) {
            session.remove(i);
        } 
        tokens.clear();
        
    }

    /**
     * Deletes the user's account
     * @param 
     * @return
     */
    public void deleteUser () {

    }

    public UUID getId () {
        return id;
    }

    private void setId (UUID id) {
        this.id = id;
    }

    public String getEmail () {
        return email;
    }

    private void setEmail (String email) {
        this.email = email;
    }

    public String getFirstName () {
        return firstName;
    }

    private void setFirstName (String firstName) {
        this.firstName = firstName;
    }

    public String getLastName () {
        return lastName;
    }

    private void setLastName (String lastName) {
        this.lastName = lastName;
    }

    private char[] getPasswordHash () {
        return passwordHash;
    }

    private void setPasswordHash (char[] hash) {
        this.passwordHash = hash;
    }

    public String getUsername () {
        return username;
    }

    private void setUsername (String username) {
        this.username = username;
    }

    public LocalDate getDob () {
        return dob;
    }

    private void setDob (LocalDate dob) {
        this.dob = dob;
    }

    private boolean isHost () {
        return isHost;
    }

    private void setHost (boolean host) {
        isHost = host;
    }

    public Set<Event> getHostingEvents () {
        return hostingEvents;
    }

    private void setHostingEvents (Set<Event> hostingEvents) {
        this.hostingEvents = hostingEvents;
    }

    public void addHostingEvent (Event event) {
        hostingEvents.add(event);
    }

    private Set<Event> getAdminEvents () {
        return adminEvents;
    }

    private void setAdminEvents (Set<Event> adminEvents) {
        this.adminEvents = adminEvents;
    }

    public void addAdminEvents (Event event) {
        this.adminEvents.add(event);
    }

    private Set<Group> getOwnedGroups () {
        return ownedGroups;
    }

    private void setOwnedGroups (Set<Group> ownedGroups) {
        this.ownedGroups = ownedGroups;
    }

    private Set<Group> getGroups () {
        return groups;
    }

    private void setGroups (Set<Group> groups) {
        this.groups = groups;
    }

    public Set<Ticket> getTickets () {
        return tickets;
    }

    private void setTickets (Set<Ticket> tickets) {
        this.tickets = tickets;
    }

    private Set<Comment> getComments () {
        return comments;
    }

    private void setComments (Set<Comment> comments) {
        this.comments = comments;
    }

    private Set<Reaction> getReactions () {
        return reactions;
    }

    private void setReactions (Set<Reaction> reactions) {
        this.reactions = reactions;
    }

    public Set<AuthToken> getTokens () {
        return tokens;
    }

    public void setTokens (Set<AuthToken> tokens) {
        this.tokens = tokens;
    }

    private boolean doReminders () {
        return reminders;
    }

    private void setReminders (boolean reminders) {
        this.reminders = reminders;
    }

    private String getDescription () {
        return description;
    }

    private void setDescription (String description) {
        this.description = description;
    }

    private String getProfilePicture () {
        return profilePicture;
    }

    private void setProfilePicture (String profilePicture) {
        this.profilePicture = profilePicture;
    }

    public NotificationManagement.Settings getSettings () {
        return new NotificationManagement.Settings(doReminders());
    }

    public void setSettings (NotificationManagement.Settings settings) {
        if (settings.reminders != null) {
            setReminders(settings.reminders);
        }
    }

    public ViewProfileResponse getProfile () {
        return new ViewProfileResponse(getUsername(), getFirstName(), getLastName(), getProfilePicture(), getEmail(), getDescription());
    }

    public void editProfile (String username, String firstName, String lastName, String email, String description, String pfpUrl) {
        if (username != null) {
            this.username = username;
        }

        if (firstName != null) {
            this.firstName = firstName;
        }

        if (lastName != null) {
            this.lastName = lastName;
        }

        if (email != null) {
            this.email = email;
        }

        if (description != null) {
            this.description = description;
        }

        if (pfpUrl != null) {
            if (!getProfilePicture().equals("")) {
                FileHelper.deleteFileAtUrl(getProfilePicture());
            }
            this.profilePicture = pfpUrl;
        }
    }

    public void invalidateToken (ModelSession session, AuthToken token) {
        getTokens().remove(token);
        session.remove(token);
    }

    public void onDelete (ModelSession session) {
        if (profilePicture != null) {
            FileHelper.deleteFileAtUrl(profilePicture);
        }
    }

    public Set<String> getTicketIds () {
        Set<String> tickets = new HashSet<>();
        for (Ticket ticket : this.tickets) {
            tickets.add(ticket.getId().toString());
        }
        return tickets; 
    }

    public List<String> getHostingEventIds () {
        List<Event> hostingEvents = new ArrayList<>(this.hostingEvents);
        List<String> eventIds = new ArrayList<>();
        for (Event event : hostingEvents) {
            eventIds.add(event.getId().toString());
        }
        Collections.sort(eventIds);
        return eventIds;
    }

    // public List<String> getPaginatedHostedEvents (int pageStart, int maxResults) {
    //     if (pageStart < 0 || maxResults <= 0) {
    //         throw new BadRequestException("Invalid paging values!");
    //     }
    //     return getHostingEventIds().subList(pageStart, Math.min(maxResults + pageStart, getHostingEventIds().size()));
    // }

    public Stream<Event> getStreamHostingEvents() {
        return getHostingEvents().stream();
    }

    public void sendEmail (String subject, String message) {
        ApiLocator.locateApi(IEmailAPI.class).sendEmail(email, subject, message);
    }
}
