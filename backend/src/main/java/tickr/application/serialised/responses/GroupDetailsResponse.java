package tickr.application.serialised.responses;

import java.util.List;

import com.google.gson.annotations.SerializedName;

public class GroupDetailsResponse {
    @SerializedName("host_id")
    public String hostId;

    public List<Users> users; 

    @SerializedName("available_reserves")
    public List<String> availableReserves;
        
    public GroupDetailsResponse(String hostId, List<Users> users, List<String> availableReserves) {
        this.hostId = hostId;
        this.users = users;
        this.availableReserves = availableReserves;
    }

    static public class Users {
        @SerializedName("user_id")
        public String userId;

        public String email;

        public String section;

        @SerializedName("seat_number")
        public int seatNumber;

        public boolean accepted;

        public Users(String userId, String email, String section, int seatNumber, boolean accepted) {
            this.userId = userId;
            this.email = email;
            this.section = section;
            this.seatNumber = seatNumber;
            this.accepted = accepted;
        }

        public Users(String section, int seatNumber, boolean accepted) {
            this.section = section;
            this.seatNumber = seatNumber;
            this.accepted = accepted;
        }
    }
}
