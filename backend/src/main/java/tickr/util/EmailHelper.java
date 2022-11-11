package tickr.util;

import tickr.application.entities.Event;
import tickr.application.entities.User;

public class EmailHelper {
    public static void sendAnnouncement (User sender, User recipient, Event event, String announcement) {
        String profileLink = String.format("http://localhost:3000/view_profile/%s", sender.getId());
        String eventLink = String.format("http://localhost:3000/view_event/%s", event.getId());
        String subject = String.format("%s announcement", event.getEventName());
        String body = String.format("<a href=\"%s\">%s %s</a> made an announcement in <a href=\"%s\">%s</a>:<br>\"%s\"", profileLink,
                sender.getFirstName(), sender.getLastName(), eventLink, event.getEventName(), announcement);

        recipient.sendEmail(subject, body);
    }
}