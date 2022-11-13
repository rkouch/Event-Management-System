package tickr.application.entities;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;

import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "categories")
public class Category {
    private static final List<String> VALID_CATEGORIES = List.of(
            "Food",
            "Music",
            "Travel & Outdoor",
            "Health",
            "Sport & Fitness",
            "Hobbies",
            "Business",
            "Free",
            "Tourism",
            "Education"
    );
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

    public String getCategory () {
        return category;
    }

    private void setCategory (String category) {
        this.category = category;
    }

    public static List<String> getValidCategories () {
        return VALID_CATEGORIES;
    }
}
