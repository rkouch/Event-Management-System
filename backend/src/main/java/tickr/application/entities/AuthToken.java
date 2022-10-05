package tickr.application.entities;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;
import tickr.util.CryptoHelper;

import java.sql.Date;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Entity
@Table(name = "auth_token")
public class AuthToken {
    @Id
    @UuidGenerator
    @JdbcTypeCode(SqlTypes.CHAR)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "issue_time")
    private LocalDateTime issueTime;

    @Column(name = "expiry_time")
    private LocalDateTime expiryTime;

    public AuthToken () {

    }

    public AuthToken (User user, LocalDateTime issueTime, Duration expiryDuration) {
        this.user = user;
        this.issueTime = issueTime.truncatedTo(ChronoUnit.SECONDS);
        this.expiryTime = issueTime.plus(expiryDuration).truncatedTo(ChronoUnit.SECONDS);
    }

    private UUID getId () {
        return id;
    }

    private void setId (UUID id) {
        this.id = id;
    }

    public User getUser () {
        return user;
    }

    private void setUser (User user) {
        this.user = user;
    }

    private LocalDateTime getIssueTime () {
        return issueTime;
    }

    private void setIssueTime (LocalDateTime issueTime) {
        this.issueTime = issueTime;
    }

    private LocalDateTime getExpiryTime () {
        return expiryTime;
    }

    private void setExpiryTime (LocalDateTime expiryTime) {
        this.expiryTime = expiryTime;
    }

    public String makeJWT () {
        return CryptoHelper.makeJWTBuilder()
                .setSubject(getUser().getId().toString())
                .setId(getId().toString())
                .setIssuedAt(Date.from(getIssueTime().atZone(ZoneId.of("UTC")).toInstant()))
                .setExpiration(Date.from(getExpiryTime().atZone(ZoneId.of("UTC")).toInstant()))
                .compact();
    }
}
