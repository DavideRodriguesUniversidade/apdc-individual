package pt.unl.fct.di.apdc.firstwebapp.util;

public class LoginData {

    public String username;
    public String password;
    public String role;  // Added role field

    public LoginData() {}

    public LoginData(String username, String password, String role) {
        this.username = username;
        this.password = password;
        this.role = role;
    }

}
