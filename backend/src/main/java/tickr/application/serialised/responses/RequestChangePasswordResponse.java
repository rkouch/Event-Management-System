package tickr.application.serialised.responses;
 
public class RequestChangePasswordResponse {
    public Boolean success;
   
    public RequestChangePasswordResponse () {
 
    }
 
    public RequestChangePasswordResponse (Boolean success) {
        this.success = success;
    }
}
