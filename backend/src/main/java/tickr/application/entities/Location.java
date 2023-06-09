package tickr.application.entities;

import jakarta.persistence.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;
import tickr.application.apis.ApiLocator;
import tickr.application.apis.location.ILocationAPI;
import tickr.application.apis.location.LocationPoint;
import tickr.application.apis.location.LocationRequest;

import java.text.DecimalFormat;
import java.util.UUID;

@Entity
@Table(name = "locations")
public class Location {
    static Logger logger = LogManager.getLogger();
    @Id
    @UuidGenerator
    @JdbcTypeCode(SqlTypes.CHAR)
    private UUID id;

    @Column(name = "street_no")
    private int streetNo;

    @Column(name = "street_name")
    private String streetName;

    private String suburb;

    @Column(name = "unit_no")
    private String unitNo;

    private String postcode;

    private String state;

    private String country;

    private Double longitude;
    private Double latitude;

    @OneToOne(fetch = FetchType.LAZY, mappedBy = "location")
    private Event event;

    public Location() {}

    public Location(int streetNo, String streetName, String unitNo, String postcode, String suburb, String state, String country) {
        this.streetNo = streetNo;
        this.streetName = streetName;
        this.unitNo = unitNo;
        this.postcode = postcode;
        this.state = state;
        this.country = country;
        this.suburb = suburb;
        this.longitude = null;
        this.latitude = null;
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
        return longitude != null ? new DecimalFormat("#.#######").format(Math.toDegrees(longitude)) : null;
    }

    private void setLongitude (String longitude) {
        this.longitude = Double.valueOf(longitude);
    }

    public String getLatitude () {
        return latitude != null ? new DecimalFormat("#.#######").format(Math.toDegrees(latitude)) : null;
    }

    private void setLatitude (String latitude) {
        this.latitude = Double.valueOf(latitude);
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

    public void lookupLongitudeLatitude () {
        var uuid = UUID.fromString(id.toString());
        var locationAPI = ApiLocator.locateApi(ILocationAPI.class);

        var request = new LocationRequest()
                .withStreetNum(streetNo)
                .withStreetName(streetName)
                .withCity(suburb)
                .withPostcode(postcode)
                .withState(state)
                .withCountry(country);

        locationAPI.getLocationAsync(request,
                ((session, locationPoint) -> session.getById(Location.class, uuid).ifPresent(l -> l.setLongitudeLatitude(locationPoint))), 300);
    }

    public void setLongitudeLatitude (LocationPoint point) {
        if (point == null) {
            logger.warn("Failed to get longitude and latitude for location {}!", id);
        } else {
            this.longitude = point.getLongitude();
            this.latitude = point.getLatitude();
        }
    }

    public double getDistance (LocationPoint point) {
        if (point == null) {
            return -1;
        } else if (latitude == null || longitude == null) {
            return Double.POSITIVE_INFINITY;
        } else {
            var d = point.getDistance(new LocationPoint(latitude, longitude));
            return d;
        }
    }

    public double getDistance (Location other) {
        if (latitude == null || longitude == null || other.getLatitude() == null || other.getLongitude() == null) {
            return Double.POSITIVE_INFINITY;
        } else {
            return new LocationPoint(latitude, longitude).getDistance(new LocationPoint(other.getLatitude(), other.getLongitude()));
        }
    }
}
