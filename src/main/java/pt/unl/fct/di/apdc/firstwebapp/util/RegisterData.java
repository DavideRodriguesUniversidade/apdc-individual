package pt.unl.fct.di.apdc.firstwebapp.util;


public class RegisterData {

    public String username;
    public String password;
    public String name;
    public String email;
    public String phone;
    public String profile;
    public String occupation;
    public String location;
    public String household;
    public String cp;
    public String nif;
    public String photo; 
    public String role;
    public String state;

    public RegisterData() {}

    public RegisterData(String username, String email, String name, String phone,
                        String password, String profile, String occupation, String location, String household,
                        String cp, String nif, String photo, String role, String state) {  // Change the parameter type to Blob
        this.username = username;
        this.email = email;
        this.name = name;
        this.phone = phone;
        this.password = password;
        this.profile = profile;
        this.occupation = occupation;
        this.location = location;
        this.household = household;
        this.cp = cp;
        this.nif = nif;
        this.photo = photo;  
        this.role = role;
        this.state = state;
    }

    public boolean validRegistration() {
        // TODO: Implement validation logic
        return true;
    }
}