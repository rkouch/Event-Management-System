package tickr.application.serialised.requests;
 
public class UserRequestChangePasswordRequest {
    public String email;
 
    public boolean isValid () {
        return email != null;
    }

    UserRequestChangePasswordRequest () {

    }

    UserRequestChangePasswordRequest(String email){
        this.email = email;
    }
}

