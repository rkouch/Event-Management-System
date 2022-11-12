package tickr.application.serialised.responses;

import com.google.gson.annotations.SerializedName;

public class ReserveDetailsResponse {
    public String section;

    @SerializedName("seat_number")
    public int seatNum;

    public float price;

    public ReserveDetailsResponse(String section, int seatNum, float price) {
        this.section = section;
        this.seatNum = seatNum;
        this.price = price;
    }
}
