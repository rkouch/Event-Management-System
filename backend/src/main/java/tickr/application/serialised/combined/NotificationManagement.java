package tickr.application.serialised.combined;

public class NotificationManagement {
    public static class GetResponse {
        public Settings settings;
    }

    public static class UpdateRequest {
        public String authToken = null;
        public Settings settings = null;

        public UpdateRequest () {

        }

        public UpdateRequest (String authToken, Settings settings) {

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
