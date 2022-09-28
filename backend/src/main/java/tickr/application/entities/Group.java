package tickr.application.entities;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "groups")
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

    private UUID getId () {
        return id;
    }

    private void setId (UUID id) {
        this.id = id;
    }

    private User getLeader () {
        return leader;
    }

    private void setLeader (User leader) {
        this.leader = leader;
    }

    private int getSize () {
        return size;
    }

    private void setSize (int size) {
        this.size = size;
    }

    private LocalDateTime getTimeCreated () {
        return timeCreated;
    }

    private void setTimeCreated (LocalDateTime timeCreated) {
        this.timeCreated = timeCreated;
    }

    private int getTicketsAvailable () {
        return ticketsAvailable;
    }

    private void setTicketsAvailable (int ticketsAvailable) {
        this.ticketsAvailable = ticketsAvailable;
    }

    private Set<User> getUsers () {
        return users;
    }

    private void setUsers (Set<User> users) {
        this.users = users;
    }
}
