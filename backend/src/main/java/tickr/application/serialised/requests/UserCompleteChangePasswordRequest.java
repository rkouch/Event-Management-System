package tickr.application.serialised.requests;

import com.google.gson.annotations.SerializedName;
 
public class UserCompleteChangePasswordRequest {
    @SerializedName("reset_token")
    public String resetToken = "";
 
    @SerializedName("password")
    public String password;
 
    @SerializedName("new_password")
    public String newPassword;
 
    public boolean isValid () {
        return password != null && newPassword != null && resetToken != null;
    }
 
    public UserCompleteChangePasswordRequest () {
 
    }
 
    public UserCompleteChangePasswordRequest (String password, String newPassword, String resetToken){
        this.resetToken = resetToken;
        this.password = password;
        this.newPassword = newPassword;
    }
}