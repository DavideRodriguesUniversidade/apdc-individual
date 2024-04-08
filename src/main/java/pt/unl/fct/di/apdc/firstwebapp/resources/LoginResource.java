package pt.unl.fct.di.apdc.firstwebapp.resources;

import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.CookieParam;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import javax.ws.rs.core.HttpHeaders;
import java.util.Map;


import com.google.appengine.repackaged.org.apache.commons.codec.digest.DigestUtils;
import com.google.cloud.Timestamp;
import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Key;
import com.google.cloud.datastore.Transaction;
import com.google.gson.Gson;

import pt.unl.fct.di.apdc.firstwebapp.authentication.SignatureUtils;
import pt.unl.fct.di.apdc.firstwebapp.util.AuthToken;
import pt.unl.fct.di.apdc.firstwebapp.util.AuthTokenManager;
import pt.unl.fct.di.apdc.firstwebapp.util.LoginData;
import pt.unl.fct.di.apdc.firstwebapp.util.RegisterData;;

@Path("/login")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class LoginResource {
    
    private static final Logger LOG = Logger.getLogger(LoginResource.class.getName());

	private static final String key = "dhsjfhndkjvnjdsdjhfkjdsjfjhdskjhfkjsdhfhdkjhkfajkdkajfhdkmc";	

    private final Gson g = new Gson();
    private final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();

    public LoginResource() {}
    
    @POST
    @Path("/v1")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response doLoginV1(LoginData data) {
        LOG.fine("Attempt to login user: " + data.username);
        
        Key userKey = datastore.newKeyFactory().setKind("User").newKey(data.username);
        
        Entity user = datastore.get(userKey);
        if(user != null) {
            String userState = (String) user.getString("user_state");
            
            // Check if user state is "INATIVO"
            if ("INATIVO".equals(userState)) {
                LOG.warning("Inactive user tried to login: " + data.username);
                return Response.status(Status.FORBIDDEN).entity("User is inactive.").build();
            }
            
            String hashedPWD = (String) user.getString("user_pwd");
            if(hashedPWD.equals(DigestUtils.sha512Hex(data.password))) {
                
                LOG.info("User " + data.username + " logged in successfully.");
                AuthToken token = new AuthToken(data.username);
                
                // Retrieve the user's role
                String userRole = (String) user.getString("user_role");
                token.setRole(userRole); // set the user role in the token

                AuthTokenManager.addToken(token.getTokenID(), token);

                
                // Create cookies
                NewCookie authTokenCookie = new NewCookie("authToken", token.getTokenID(), "/", null, "comment", 60 * 60 * 2, false, false);
                NewCookie authTokenRoleCookie = new NewCookie("authToken", token.getRole(), "/", null, "comment", 60 * 60 * 2, false, false);
                NewCookie usernameCookie = new NewCookie("username", data.username, "/", null, "comment", 60 * 60 * 2, false, false);
                NewCookie userRoleCookie = new NewCookie("userRole", userRole, "/", null, "comment", 60 * 60 * 2, false, false);
                
                return Response.ok(g.toJson(token)).cookie(authTokenCookie, authTokenRoleCookie, usernameCookie, userRoleCookie).build();
            } else {
                LOG.warning("Wrong password for: " + data.username);
                return Response.status(Status.FORBIDDEN).entity("Wrong password.").build();
            }
        } else {
            LOG.warning("Failed login attempt for username: " + data.username);
            return Response.status(Status.FORBIDDEN).entity("User does not exist.").build();
        }
    }
    
    @POST
    @Path("/checkPermissions")
    public Response checkSessionPermissions(@CookieParam("sessionToken") String authTokenID) {
        AuthToken token = AuthTokenManager.getToken(authTokenID);
        boolean valid = checkPermissions(token);
        if(valid) {
            return Response.ok("true").build();
        } else {
            return Response.ok("false").build();
        }
    }
    
    public static boolean checkPermissions(AuthToken token) {
		if (token == null) {
			return false;
		}

		String value = token.getTokenID();
		String[] values = value.split("\\.");
	
		String signatureNew = SignatureUtils.calculateHMac(key, values[0]+"."+values[1]+"."+values[2]+"."+values[3]+"."+values[4]);
		String signatureOld = values[5];
					
		if(!signatureNew.equals(signatureOld)) {
			return false;
		}
		
		if(System.currentTimeMillis() > (Long.valueOf(values[3]) + Long.valueOf(values[4]))) {
			
			return false;
		}	
		
		return true;
	}
}
