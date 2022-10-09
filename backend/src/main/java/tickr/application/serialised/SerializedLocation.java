package tickr.application.serialised;

public class SerializedLocation {
    public String streetName; 
    public int streetNo;
    public String unitNo;
    public String postcode; 
    public String state;
    public String country;
    public String longitude;
    public String latitude;

    public SerializedLocation() {
    }

    public SerializedLocation(String streetName, int streetNo, String unitNo, String postcode, String state, String country, String longitude, String latitude) {
        this.streetName = streetName;
        this.streetNo = streetNo;
        this.unitNo = unitNo;
        this.postcode = postcode;
        this.state = state;
        this.country = country;
        this.longitude = longitude; 
        this.latitude = latitude;
    } 


}
