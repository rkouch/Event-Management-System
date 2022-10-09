package tickr.application.entities;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;

import java.util.UUID;

@Entity
@Table(name = "categories")
public class Category {
    @Id
    @UuidGenerator
    @JdbcTypeCode(SqlTypes.CHAR)
    private UUID id;

    //@Column(name = "event_id")
    //private int eventId;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id")
    private Event event;

    private String category;

    public Category () {}

    public Category (String category) {
        this.category = category; 
    }

    private UUID getId () {
        return id;
    }

    private void setId (UUID id) {
        this.id = id;
    }

    private Event getEvent () {
        return event;
    }

    public void setEvent (Event event) {
        this.event = event;
    }

    private String getCategory () {
        return category;
    }

    private void setCategory (String category) {
        this.category = category;
    }
}
