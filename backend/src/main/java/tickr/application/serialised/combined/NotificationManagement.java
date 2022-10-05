package tickr.application.serialised.combined;

public class NotificationManagement {
    public static class GetResponse {
        public Settings settings;

        public GetResponse () {

        }

        public GetResponse (Settings settings) {
            this.settings = settings;
        }

    }

    public static class UpdateRequest {
        public String authToken = null;
        public Settings settings = null;

        public UpdateRequest () {

        }

        public UpdateRequest (String authToken, Settings settings) {
            this.authToken = authToken;
            this.settings = settings;
        }
    }

    public static class Settings {
        public Boolean reminders = null;

        public Settings () {

        }

        public Settings (boolean reminders) {
            this.reminders = reminders;
        }
    }
}
