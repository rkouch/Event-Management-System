package tickr.application.serialised.responses;

public class CreateEventResponse {
    public String event_id; 

    public CreateEventResponse () {}

    public CreateEventResponse (String event_id) {
        this.event_id = event_id; 
    }
}
