package tickr.application.entities;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "user_groups")
public class Group {
    @Id
    @UuidGenerator
    @JdbcTypeCode(SqlTypes.CHAR)
    private UUID id;

    //@Column(name = "leader_id")
    //private int leaderId;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "leader_id")
    private User leader;

    private int size;

    @Column(name = "time_created")
    private LocalDateTime timeCreated;

    @Column(name = "ticket_available")
    private int ticketsAvailable;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "group_users",
            joinColumns = {@JoinColumn(name = "group_id")},
            inverseJoinColumns = {@JoinColumn(name = "user_id")})
    private Set<User> users;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "group", cascade = CascadeType.REMOVE)
    private Set<TicketReservation> ticketReservations;


    public Group(User leader, LocalDateTime timeCreated, int size, Set<TicketReservation> ticketReservations) {
        this.leader = leader;
        this.timeCreated = timeCreated;
        this.size = size;
        this.ticketsAvailable = ticketReservations.size();
        this.ticketReservations = ticketReservations;
        this.users = new HashSet<>();
    }

    public Group()  {}

    public UUID getId () {
        return id;
    }

    private void setId (UUID id) {
        this.id = id;
    }

    public User getLeader () {
        return leader;
    }

    private void setLeader (User leader) {
        this.leader = leader;
    }

    public int getSize () {
        return size;
    }

    private void setSize (int size) {
        this.size = size;
    }

    public LocalDateTime getTimeCreated () {
        return timeCreated;
    }

    private void setTimeCreated (LocalDateTime timeCreated) {
        this.timeCreated = timeCreated;
    }

    public int getTicketsAvailable () {
        return ticketsAvailable;
    }

    private void setTicketsAvailable (int ticketsAvailable) {
        this.ticketsAvailable = ticketsAvailable;
    }

    public Set<User> getUsers () {
        return users;
    }

    private void setUsers (Set<User> users) {
        this.users = users;
    }

    public void addUser(User user) {
        this.users.add(user);
    }

    public Set<TicketReservation> getTicketReservations() {
        return ticketReservations;
    }

    public void setTicketReservations(Set<TicketReservation> ticketReservations) {
        this.ticketReservations = ticketReservations;
    }

    public void addGroupToTicketReservations() {
        for (TicketReservation t : ticketReservations) {
            t.setGroup(this);
        }
    }
}
