package pt.unl.fct.di.apdc.firstwebapp.util;

public class ChangePasswordData {
    public String username;
    public String currentPassword;
    public String newPassword;
    public String confirmNewPassword;

    public ChangePasswordData(String username, String currentPassword, String newPassword, String confirmNewPassword) {
        this.username = username;
        this.currentPassword = currentPassword;
        this.newPassword = newPassword;
        this.confirmNewPassword = confirmNewPassword;
    }

    // Default constructor for JSON deserialization
    public ChangePasswordData() {}
}
