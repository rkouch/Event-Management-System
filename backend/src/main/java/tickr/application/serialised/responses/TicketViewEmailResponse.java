package tickr.application.serialised.responses;

public class TicketViewEmailResponse {
    public boolean success;

    public TicketViewEmailResponse(boolean success) {
        this.success = success;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    } 

    
}
