package tickr.application.entities;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "event_comments")
public class Comment {
    @Id
    @UuidGenerator
    @JdbcTypeCode(SqlTypes.CHAR)
    private UUID id;

    //@Column(name = "event_id")
    //private int eventId;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id")
    private Event event;

    //@Column(name = "parent_id")
    //private int parentId;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Comment parent;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "parent")
    private Set<Comment> children;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "comment", cascade = CascadeType.REMOVE)
    private Set<Reaction> reactions;

    //@Column(name = "author_id")
    //private int authorId;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id")
    private User author;

    @Column(name = "comment_text")
    private String commentText;

    @Column(name = "comment_time")
    private LocalDateTime commentTime;

    private UUID getId () {
        return id;
    }

    private void setId (UUID id) {
        this.id = id;
    }

    private Event getEvent () {
        return event;
    }

    private void setEvent (Event event) {
        this.event = event;
    }

    private Comment getParent () {
        return parent;
    }

    private void setParent (Comment parent) {
        this.parent = parent;
    }

    private Set<Comment> getChildren () {
        return children;
    }

    private void setChildren (Set<Comment> children) {
        this.children = children;
    }

    private Set<Reaction> getReactions () {
        return reactions;
    }

    private void setReactions (Set<Reaction> reactions) {
        this.reactions = reactions;
    }

    private User getAuthor () {
        return author;
    }

    private void setAuthor (User author) {
        this.author = author;
    }

    private String getCommentText () {
        return commentText;
    }

    private void setCommentText (String commentText) {
        this.commentText = commentText;
    }

    private LocalDateTime getCommentTime () {
        return commentTime;
    }

    private void setCommentTime (LocalDateTime commentTime) {
        this.commentTime = commentTime;
    }
}
