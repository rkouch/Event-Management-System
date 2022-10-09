package tickr.application.serialised;

import com.google.gson.annotations.SerializedName;

public class SerializedLocation {
    @SerializedName ("street_name")
    public String streetName; 

    @SerializedName ("street_no")
    public int streetNo;

    @SerializedName ("unit_no")
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
