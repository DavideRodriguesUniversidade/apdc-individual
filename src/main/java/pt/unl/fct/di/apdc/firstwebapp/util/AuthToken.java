package pt.unl.fct.di.apdc.firstwebapp.util;

import java.util.UUID;

public class AuthToken {
    
    public static final long EXPIRATION_TIME = 1000*60*60*2; //2h
    
    public String username;
    public String role;
    public String tokenID;
    public long validFrom;
    public long validTo;
    public String verifier;

    public AuthToken(String username) {
        this.username = username;
        this.tokenID = UUID.randomUUID().toString();
        this.validFrom = System.currentTimeMillis();
        this.validTo = this.validFrom + AuthToken.EXPIRATION_TIME;
        this.verifier = UUID.randomUUID().toString(); // generate a random verifier
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getTokenID() {
        return this.tokenID;
    }
    
    public String getRole() {
        return this.role;
    }
    
    public String getUsername() {
        return this.username;
    }

}
