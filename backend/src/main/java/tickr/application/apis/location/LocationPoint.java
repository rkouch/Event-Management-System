package tickr.application.apis.location;

public class LocationPoint {
    private static final String POINT_SEPARATOR = ",";
    private final String latitude;
    private final String longitude;

    public LocationPoint (String latitude, String longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getLatitude () {
        return latitude;
    }

    public String getLongitude () {
        return longitude;
    }

    public String serialise () {
        return latitude + POINT_SEPARATOR + longitude;
    }
    public static LocationPoint deserialise (String serialisedPoint) {
        var pointParts = serialisedPoint.split(POINT_SEPARATOR);
        return new LocationPoint(pointParts[0], pointParts[1]);
    }
}
