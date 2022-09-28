package tickr.application.entities;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.type.SqlTypes;
import org.hibernate.usertype.UserTypeLegacyBridge;

import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "TestTable")
public class TestEntity {
    @Id
    @UuidGenerator
    @JdbcTypeCode(SqlTypes.CHAR)
    private UUID id;

    private String name;
    
    private String email;

    public TestEntity () {

    }

    public TestEntity (String name, String email) {
        this.name = name;
        this.email = email;
    }


    public UUID getId () {
        return id;
    }

    public void setId (UUID id) {
        this.id = id;
    }


    public String getName () {
        return name;
    }

    public void setName (String name) {
        this.name = name;
    }

    public String getEmail () {
        return email;
    }

    public void setEmail (String email) {
        this.email = email;
    }

    @Override
    public boolean equals (Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TestEntity that = (TestEntity) o;
        return Objects.equals(id, that.id) && Objects.equals(name, that.name) && Objects.equals(email, that.email);
    }

    @Override
    public int hashCode () {
        return Objects.hash(id, name, email);
    }
}
