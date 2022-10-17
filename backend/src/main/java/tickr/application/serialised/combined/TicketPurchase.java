package tickr.application.serialised.combined;

import com.google.gson.annotations.SerializedName;

public class TicketPurchase {
    public static class Request {
        @SerializedName("auth_token")
        public String authToken;

        @SerializedName("reserve_id")
        public String reserveId;

        @SerializedName("success_url")
        public String successUrl;

        @SerializedName("cancel_url")
        public String cancelUrl;

        public Request () {}

        public Request (String authToken, String reserveId, String successUrl, String cancelUrl) {
            this.authToken = authToken;
            this.reserveId = reserveId;
            this.successUrl = successUrl;
            this.cancelUrl = cancelUrl;
        }
    }

    public static class Response {
        @SerializedName("redirect_url")
        public String redirectUrl;

        public Response () {}

        public Response (String redirectUrl) {
            this.redirectUrl = redirectUrl;
        }
    }
}
