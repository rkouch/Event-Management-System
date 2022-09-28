package tickr.application.entities;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;

import java.util.UUID;

@Entity
@Table(name = "locations")
public class Location {
    @Id
    @UuidGenerator
    @JdbcTypeCode(SqlTypes.CHAR)
    private UUID id;

    @Column(name = "street_no")
    private int streetNo;

    @Column(name = "unit_no")
    private String unitNo;

    private String postcode;

    private String state;

    private String country;

    private String longitude;
    private String latitude;

    @OneToOne(fetch = FetchType.LAZY, mappedBy = "location")
    private Event event;

    private UUID getId () {
        return id;
    }

    private void setId (UUID id) {
        this.id = id;
    }

    private int getStreetNo () {
        return streetNo;
    }

    private void setStreetNo (int streetNo) {
        this.streetNo = streetNo;
    }

    private String getUnitNo () {
        return unitNo;
    }

    private void setUnitNo (String unitNo) {
        this.unitNo = unitNo;
    }

    private String getPostcode () {
        return postcode;
    }

    private void setPostcode (String postcode) {
        this.postcode = postcode;
    }

    private String getState () {
        return state;
    }

    private void setState (String state) {
        this.state = state;
    }

    private String getCountry () {
        return country;
    }

    private void setCountry (String country) {
        this.country = country;
    }

    private String getLongitude () {
        return longitude;
    }

    private void setLongitude (String longitude) {
        this.longitude = longitude;
    }

    private String getLatitude () {
        return latitude;
    }

    private void setLatitude (String latitude) {
        this.latitude = latitude;
    }

    private Event getEvent () {
        return event;
    }

    private void setEvent (Event event) {
        this.event = event;
    }
}
