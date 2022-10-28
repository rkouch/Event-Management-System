package tickr.application.entities;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "reactions")
public class Reaction {
    @Id
    @UuidGenerator
    @JdbcTypeCode(SqlTypes.CHAR)
    private UUID id;

    //@Column(name = "comment_id")
    //private int commentId;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comment_id")
    private Comment comment;

    //@Column(name = "author_id")
    //private int authorId;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id")
    private User author;

    @Column(name = "react_time")
    private LocalDateTime reactTime;

    @Column(name = "react_type")
    private String reactType;

    private UUID getId () {
        return id;
    }

    private void setId (UUID id) {
        this.id = id;
    }

    private Comment getComment () {
        return comment;
    }

    private void setComment (Comment comment) {
        this.comment = comment;
    }

    private User getAuthor () {
        return author;
    }

    private void setAuthor (User author) {
        this.author = author;
    }

    private LocalDateTime getReactTime () {
        return reactTime;
    }

    private void setReactTime (LocalDateTime reactTime) {
        this.reactTime = reactTime;
    }

    public String getReactType () {
        return reactType;
    }

    private void setReactType (String reactType) {
        this.reactType = reactType;
    }
}
