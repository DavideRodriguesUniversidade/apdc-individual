package pt.unl.fct.di.apdc.firstwebapp.util;

import java.util.HashMap;

import pt.unl.fct.di.apdc.firstwebapp.util.AuthToken;

public class AuthTokenManager {

    private static final HashMap<String, AuthToken> tokenMap = new HashMap<>();

    
    /**
     * Retrieves an AuthToken based on its token ID.
     *
     * @param tokenID The token ID of the AuthToken to retrieve.
     * @return The retrieved AuthToken, or null if not found.
     */
    public static AuthToken getToken(String tokenID) {
    	AuthToken token = tokenMap.get(tokenID);
            return token;
    }
    
    /**
     * Adds a new AuthToken to the tokenMap.
     *
     * @param tokenID  The tokenID to be used as the key.
     * @param authToken The AuthToken object to be stored.
     */
    public static void addToken(String tokenID, AuthToken authToken) {
        tokenMap.put(tokenID, authToken);
    }

    /**
     * Validates and retrieves an AuthToken from the tokenMap.
     *
     * @param tokenID The tokenID to be used for retrieval.
     * @return The AuthToken object if valid and present, otherwise null.
     */
    public static AuthToken validateAndRetrieveToken(String tokenID) {
        AuthToken token = tokenMap.get(tokenID);
        
        if (token != null && System.currentTimeMillis() <= token.validTo) {
            return token;
        }
        
        return null;
    }

    /**
     * Removes an AuthToken from the tokenMap.
     *
     * @param tokenID The tokenID of the AuthToken to be removed.
     */
    public static void removeToken(String tokenID) {
        tokenMap.remove(tokenID);
    }
}
