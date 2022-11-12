package tickr.application.entities;

import jakarta.persistence.*;
import tickr.application.serialised.responses.GroupDetailsResponse.Users;
import tickr.server.exceptions.BadRequestException;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.time.ZonedDateTime;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

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
    private ZonedDateTime timeCreated;

    @Column(name = "ticket_available")
    private int ticketsAvailable;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "group_users",
            joinColumns = {@JoinColumn(name = "group_id")},
            inverseJoinColumns = {@JoinColumn(name = "user_id")})
    private Set<User> users;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "group", cascade = CascadeType.REMOVE)
    private Set<TicketReservation> ticketReservations;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "group", cascade = CascadeType.REMOVE)
    private Set<Invitation> invitations; 

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "group")
    private Set<Ticket> tickets; 

    public Group(User leader, ZonedDateTime timeCreated, int size, Set<TicketReservation> ticketReservations) {
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

    private ZonedDateTime getTimeCreated () {
        return timeCreated;
    }

    private void setTimeCreated (ZonedDateTime timeCreated) {
        this.timeCreated = timeCreated;
    }

    public int getTicketsAvailable () {
        return ticketsAvailable;
    }

    public void setTicketsAvailable (int ticketsAvailable) {
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

    public void addInvitation(Invitation invitation) {
        this.invitations.add(invitation);
    }

    public void removeInvitation(Invitation invitation) {
        invitations.remove(invitation);
    }

    public Set<Ticket> getTickets() {
        return tickets;
    }

    public void setTickets(Set<Ticket> tickets) {
        this.tickets = tickets;
    }

    public void addTickets(Ticket ticket) {
        this.tickets.add(ticket);
    }

    public void removeReservation(TicketReservation ticket) {
        this.ticketReservations.remove(ticket);
    }

    public void acceptInvitation(Invitation invitation, User user) {
        if (!getUsers().contains(user)) {
            addUser(user);
            setSize(this.size + 1);
        }
        removeInvitation(invitation);
    }

    public void convert(Ticket ticket, TicketReservation reserve) {
        addTickets(ticket);
        removeReservation(reserve);
        setTicketsAvailable(this.ticketsAvailable - 1);
        ticket.setGroup(this);
    }

    public List<Users> getUserDetails() {
        List<Users> users = new ArrayList<>();
        for (Ticket t : tickets) {
            users.add(t.createUsersDetails());
        }
        for (TicketReservation r : ticketReservations) {
            if (r.createUsersDetails() != null) {
                users.add(r.createUsersDetails());
            }
        }
        return users;
    }
    
    public List<String> getAvailableReserves(User host) {
        return  host.equals(leader) 
        ? ticketReservations.stream()
                .filter(t -> t.getInvitation() == null && !t.isGroupAccepted())
                .map(TicketReservation::getId)
                .map(UUID::toString)
                .collect(Collectors.toList())
        : null;
    }

    public void removeUser(User user) {
        if (!users.contains(user)) {
            throw new BadRequestException("User is not a part of this group!");
        } else {
            users.remove(user);
        }
        for (TicketReservation t : ticketReservations) {
            if (t.getUser().equals(user)) {
                t.removeUserFromGroup(leader);
            }
        }
    }
}
