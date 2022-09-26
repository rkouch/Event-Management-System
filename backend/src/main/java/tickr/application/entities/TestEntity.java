package tickr.application.entities;

import jakarta.persistence.*;

import java.util.Objects;

@Entity
@Table(name = "TestTable")
public class TestEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id = 0;

    private String name;
    
    private String email;

    public TestEntity () {

    }

    public TestEntity (String name, String email) {
        this.name = name;
        this.email = email;
    }


    public int getId () {
        return id;
    }

    public void setId (int id) {
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
        return id == that.id && Objects.equals(name, that.name) && Objects.equals(email, that.email);
    }

    @Override
    public int hashCode () {
        return Objects.hash(id, name, email);
    }
}
