package tickr.application.entities;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;
import tickr.persistence.ModelSession;
import tickr.server.exceptions.ForbiddenException;
import tickr.util.CryptoHelper;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

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

    @Column(name = "is_host")
    private boolean isHost;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "user")
    private Set<AuthToken> tokens = new HashSet<>();

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "host")
    private Set<Event> hostingEvents = new HashSet<>();

    //@OneToMany(fetch = FetchType.LAZY, mappedBy = "user")
    //private Set<EventAdmin> adminEvents;

    @ManyToMany(fetch = FetchType.LAZY, mappedBy = "admins")
    private Set<Event> adminEvents = new HashSet<>();

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "leader")
    private Set<Group> ownedGroups = new HashSet<>();

    @ManyToMany(fetch = FetchType.LAZY, mappedBy = "users")
    private Set<Group> groups = new HashSet<>();

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "user")
    private Set<Ticket> tickets = new HashSet<>();

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "author")
    private Set<Comment> comments = new HashSet<>();

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "author")
    private Set<Reaction> reactions = new HashSet<>();

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

    private Set<Event> getHostingEvents () {
        return hostingEvents;
    }

    private void setHostingEvents (Set<Event> hostingEvents) {
        this.hostingEvents = hostingEvents;
    }

    private Set<Event> getAdminEvents () {
        return adminEvents;
    }

    private void setAdminEvents (Set<Event> adminEvents) {
        this.adminEvents = adminEvents;
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

    private Set<Ticket> getTickets () {
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
}
