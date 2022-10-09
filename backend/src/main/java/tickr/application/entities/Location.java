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

    @Column(name = "street_name")
    private String streetName;

    @Column
    private String suburb;

    @Column(name = "unit_no")
    private String unitNo;

    private String postcode;

    private String state;

    private String country;

    private String longitude;
    private String latitude;

    @OneToOne(fetch = FetchType.LAZY, mappedBy = "location")
    private Event event;

    public Location() {}

    public Location(int streetNo, String streetName, String unitNo, String postcode, String state, String country, String longitude, String latitude) {
        this.streetNo = streetNo;
        this.streetName = streetName;
        this.unitNo = unitNo;
        this.postcode = postcode;
        this.state = state;
        this.country = country;
        this.longitude = longitude;
        this.latitude = latitude;
    }

    public UUID getId () {
        return id;
    }

    private void setId (UUID id) {
        this.id = id;
    }

    public int getStreetNo () {
        return streetNo;
    }

    private void setStreetNo (int streetNo) {
        this.streetNo = streetNo;
    }

    public String getUnitNo () {
        return unitNo;
    }

    private void setUnitNo (String unitNo) {
        this.unitNo = unitNo;
    }

    public String getPostcode () {
        return postcode;
    }

    private void setPostcode (String postcode) {
        this.postcode = postcode;
    }

    public String getState () {
        return state;
    }

    private void setState (String state) {
        this.state = state;
    }

    public String getCountry () {
        return country;
    }

    private void setCountry (String country) {
        this.country = country;
    }

    public String getLongitude () {
        return longitude;
    }

    private void setLongitude (String longitude) {
        this.longitude = longitude;
    }

    public String getLatitude () {
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

    public String getStreetName() {
        return streetName;
    }

    public void setStreetName(String streetName) {
        this.streetName = streetName;
    }

    public String getSuburb () {
        return suburb;
    }
}
