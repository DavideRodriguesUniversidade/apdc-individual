package pt.unl.fct.di.apdc.firstwebapp.resources;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.CookieParam;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.google.appengine.repackaged.org.apache.commons.codec.digest.DigestUtils;
import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Key;
import com.google.cloud.datastore.Transaction;

import pt.unl.fct.di.apdc.firstwebapp.util.AuthToken;
import pt.unl.fct.di.apdc.firstwebapp.util.AuthTokenManager;
import pt.unl.fct.di.apdc.firstwebapp.util.ChangePasswordData;
import pt.unl.fct.di.apdc.firstwebapp.util.RegisterData;

@Path("/users")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class EditUserResource {

    private static final Logger LOG = Logger.getLogger(EditUserResource.class.getName());
    private final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();

    public EditUserResource() {}

    @POST
    @Path("/remove")
    public Response removeUser(RegisterData data, @HeaderParam("authToken") String authTokenID) {
        LOG.fine("Attempt to remove user: " + data.username);

        AuthToken token = AuthTokenManager.getToken(authTokenID);
        if (token == null) {
            LOG.warning("Invalid or expired token.");
            return Response.status(Status.UNAUTHORIZED).entity("Invalid or expired token.").build();
        }

        String userRoleFromToken = token.getRole();

        try {
            Transaction txn = datastore.newTransaction();

            // SU can remove any account
            if ("SU".equals(userRoleFromToken)) {
                Key userKey = datastore.newKeyFactory().setKind("User").newKey(data.username);
                txn.delete(userKey);
                
                txn.commit();

                LOG.info("User removed: " + data.username);
                return Response.ok("{}").build();
            } 
            // GA can remove any GBO or USER account
            else if ("GA".equals(userRoleFromToken)) {
                Entity user = txn.get(datastore.newKeyFactory().setKind("User").newKey(data.username));
                String userRole = user.getString("user_role");
                if ("GBO".equals(userRole) || "USER".equals(userRole)) {
                    Key userKey = datastore.newKeyFactory().setKind("User").newKey(data.username);
                    txn.delete(userKey);
                    
                    txn.commit();

                    LOG.info("User removed: " + data.username);
                    return Response.ok("{}").build();
                } else {
                    LOG.warning("User with role " + userRoleFromToken + " is not allowed to remove user with role " + userRole);
                    return Response.status(Status.FORBIDDEN).entity("User not allowed to remove this user.").build();
                }
            } 
            // GBO can't remove accounts
            else if ("GBO".equals(userRoleFromToken)) {
                LOG.warning("User with role " + userRoleFromToken + " is not allowed to remove any user.");
                return Response.status(Status.FORBIDDEN).entity("User not allowed to remove any user.").build();
            } 
            // USER can only remove its own
            else if ("USER".equals(userRoleFromToken)) {
                if (data.username.equals(token.getUsername())) {
                    Key userKey = datastore.newKeyFactory().setKind("User").newKey(data.username);
                    txn.delete(userKey);
                    
                    txn.commit();

                    LOG.info("User removed: " + data.username);
                    return Response.ok("{}").build();
                } else {
                    LOG.warning("User with role " + userRoleFromToken + " is not allowed to remove other users.");
                    return Response.status(Status.FORBIDDEN).entity("User not allowed to remove other users.").build();
                }
            } 
            // Handle unknown roles
            else {
                LOG.warning("Unknown role: " + userRoleFromToken);
                return Response.status(Status.FORBIDDEN).entity("Unknown role.").build();
            }

        } catch (Exception e) {
            LOG.severe("Error removing user: " + e.getMessage());
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Internal Server Error.").build();
        }
    }

    
    @POST
    @Path("/updateRole")
    public Response updateUserRole(RegisterData data, @HeaderParam("authToken") String authTokenID) {
        LOG.fine("Attempt to update role for user: " + data.username);

        AuthToken token = AuthTokenManager.getToken(authTokenID); // Retrieve the AuthToken object using the authTokenID

        if (token == null) {
            LOG.warning("AuthToken not found.");
            return Response.status(Status.FORBIDDEN).entity("AuthToken not found.").build();
        }

        // Retrieve the user's role from the token
        String userRoleFromToken = token.getRole();

        // Define allowed roles based on the user's role from the token
        List<String> allowedRoles = new ArrayList<>();
        if ("SU".equals(userRoleFromToken)) {
            allowedRoles = Arrays.asList("SU", "GA", "GBO", "USER");
        } else if ("GA".equals(userRoleFromToken)) {
            allowedRoles = Arrays.asList("GBO", "USER");
        }

        // Check if the current user's role allows the operation
        if (!allowedRoles.contains(userRoleFromToken)) {
            LOG.warning("User with role " + userRoleFromToken + " is not allowed to change role to " + data.role);
            return Response.status(Status.FORBIDDEN).entity("Not allowed to change role to " + data.role).build();
        }

        Transaction txn = datastore.newTransaction();

        try {
            Key userKey = datastore.newKeyFactory().setKind("User").newKey(data.username);
            Entity user = txn.get(userKey);
            
            if(user == null) {
                txn.rollback();
                return Response.status(Status.BAD_REQUEST).entity("User not found.").build();
            }

            user = Entity.newBuilder(userKey)
                .set("user_id", data.username)
                .set("user_email", user.getString("user_email"))
                .set("user_name", user.getString("user_name"))
                .set("user_phone", user.getString("user_phone"))
                .set("user_pwd", user.getString("user_pwd"))
                .set("user_profile", user.getString("user_profile"))
                .set("user_occupation", user.getString("user_occupation"))
                .set("user_workLocation", user.getString("user_workLocation"))
                .set("user_household", user.getString("user_household"))
                .set("user_cp", user.getString("user_cp"))
                .set("user_nif", user.getString("user_nif"))
                .set("user_photo", user.getString("user_photo"))
                .set("user_role", data.role)  // Update role here
                .set("user_state", user.getString("user_state"))
                .build();

            txn.put(user);
            txn.commit();

            LOG.info("Role updated for user: " + data.username);
            return Response.ok("{}").build();
        } catch (Exception e) {
            LOG.severe("Error updating role for user: " + e.getMessage());
            if(txn.isActive()) {
                txn.rollback();
            }
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Internal Server Error.").build();
        } finally {
            if(txn.isActive()) {
                txn.rollback();
            }
        }
    }

    
    @POST
    @Path("/updateState")
    public Response updateUserState(RegisterData data, @HeaderParam("authToken") String authTokenID) {
        LOG.fine("Attempt to update state for user: " + data.username);

        AuthToken token = AuthTokenManager.getToken(authTokenID);
        if (token == null) {
            LOG.warning("Invalid or expired token.");
            return Response.status(Status.UNAUTHORIZED).entity("Invalid or expired token.").build();
        }
        
        String userRoleFromToken = token.getRole();

        Transaction txn = datastore.newTransaction();

        try {
            Key userKey = datastore.newKeyFactory().setKind("User").newKey(data.username);
            Entity user = txn.get(userKey);
            
            if(user == null) {
                txn.rollback();
                return Response.status(Status.BAD_REQUEST).entity("User not found.").build();
            }
            
            String userRole = user.getString("user_role");

            // Check permissions based on roles
            if ("SU".equals(userRoleFromToken)) {
                // SU can update any user
            } else if ("GA".equals(userRoleFromToken)) {
                // GA can update users with roles "GBO" and "USER"
                if (!("GBO".equals(userRole) || "USER".equals(userRole))) {
                    LOG.warning("User with role " + userRoleFromToken + " is not allowed to update state for user with role " + userRole);
                    return Response.status(Status.FORBIDDEN).entity("User not allowed to update state for user with role " + userRole).build();
                }
            } else if ("GBO".equals(userRoleFromToken)) {
                // GBO can update users with role "USER"
                if (!"USER".equals(userRole)) {
                    LOG.warning("User with role " + userRoleFromToken + " is not allowed to update state for user with role " + userRole);
                    return Response.status(Status.FORBIDDEN).entity("User not allowed to update state for user with role " + userRole).build();
                }
            } else if ("USER".equals(userRoleFromToken)) {
                // USER cannot update any other user
                LOG.warning("User with role " + userRoleFromToken + " is not allowed to update state.");
                return Response.status(Status.FORBIDDEN).entity("User not allowed to update state.").build();
            }

            user = Entity.newBuilder(userKey)
                .set("user_id", data.username)
                .set("user_email", user.getString("user_email"))
                .set("user_name", user.getString("user_name"))
                .set("user_phone", user.getString("user_phone"))
                .set("user_pwd", user.getString("user_pwd"))
                .set("user_profile", user.getString("user_profile"))
                .set("user_occupation", user.getString("user_occupation"))
                .set("user_workLocation", user.getString("user_workLocation"))
                .set("user_household", user.getString("user_household"))
                .set("user_cp", user.getString("user_cp"))
                .set("user_nif", user.getString("user_nif"))
                .set("user_photo", user.getString("user_photo"))
                .set("user_role", user.getString("user_role"))  // Keep the role unchanged
                .set("user_state", data.state)  // Update state here
                .build();

            txn.put(user);
            txn.commit();

            LOG.info("State updated for user: " + data.username);
            return Response.ok("{}").build();
        } catch (Exception e) {
            LOG.severe("Error updating state for user: " + e.getMessage());
            if(txn.isActive()) {
                txn.rollback();
            }
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Internal Server Error.").build();
        } finally {
            if(txn.isActive()) {
                txn.rollback();
            }
        }
    }

    
    @POST
    @Path("/updateUser")
    public Response updateUser(RegisterData data, @HeaderParam("authToken") String authTokenID) {
        LOG.fine("Attempt to update user: " + data.username);

        AuthToken token = AuthTokenManager.getToken(authTokenID);
        if (token == null) {
            LOG.warning("Invalid or expired token.");
            return Response.status(Status.UNAUTHORIZED).entity("Invalid or expired token.").build();
        }

        String userRoleFromToken = token.getRole();

        // Validate the user's role to determine which attributes can be modified
        Transaction txn = datastore.newTransaction();
        try {
            Key userKey = datastore.newKeyFactory().setKind("User").newKey(data.username);
            Entity user = txn.get(userKey);
            
            if(user == null) {
                txn.rollback();
                return Response.status(Status.BAD_REQUEST).entity("User not found.").build();
            }

            boolean isAllowed = false;

            // Role-based attribute modification validation
            if ("USER".equals(userRoleFromToken)) {
                if (data.username.equals(token.getUsername())) {
                    isAllowed = true; // USER can modify their own attributes
                }
            } else if ("GBO".equals(userRoleFromToken)) {
                if ("USER".equals(user.getString("user_role"))) {
                    isAllowed = true; // GBO can modify USER attributes
                }
            } else if ("GA".equals(userRoleFromToken)) {
                if ("GBO".equals(user.getString("user_role")) || "USER".equals(user.getString("user_role"))) {
                    isAllowed = true; // GA can modify GBO or USER attributes
                }
            } else if ("SU".equals(userRoleFromToken)) {
                isAllowed = true; // SU can modify any user's attributes
            }

            if (!isAllowed) {
                txn.rollback();
                LOG.warning("User with role " + userRoleFromToken + " is not allowed to update user: " + data.username);
                return Response.status(Status.FORBIDDEN).entity("User not allowed to update user: " + data.username).build();
            }

            // Update only the provided attribute and its value
            user = Entity.newBuilder(userKey)
                .set("user_id", data.username)
                .set("user_email", data.email != null ? data.email : user.getString("user_email"))
                .set("user_name", data.name != null ? data.name : user.getString("user_name"))
                .set("user_phone", data.phone != null ? data.phone : user.getString("user_phone"))
                .set("user_pwd", user.getString("user_pwd"))
                .set("user_profile", data.profile != null ? data.profile : user.getString("user_profile"))
                .set("user_occupation", data.occupation != null ? data.occupation : user.getString("user_occupation"))
                .set("user_workLocation", data.location != null ? data.location : user.getString("user_workLocation"))
                .set("user_household", data.household != null ? data.household : user.getString("user_household"))
                .set("user_cp", data.cp != null ? data.cp : user.getString("user_cp"))
                .set("user_nif", data.nif != null ? data.nif : user.getString("user_nif"))
                .set("user_photo", data.photo != null ? data.photo : user.getString("user_photo"))
                .set("user_role", data.role != null ? data.role : user.getString("user_role"))
                .set("user_state", data.state != null ? data.state : user.getString("user_state")) 
                .build();

            txn.put(user);
            txn.commit();

            LOG.info("Updated user: " + data.username);
            return Response.ok("{}").build();
        } catch (Exception e) {
            LOG.severe("Error updating user: " + e.getMessage());
            if(txn.isActive()) {
                txn.rollback();
            }
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Internal Server Error.").build();
        } finally {
            if(txn.isActive()) {
                txn.rollback();
            }
        }
    }

    
    @POST
    @Path("/changePassword")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response changePassword(ChangePasswordData data) {
        LOG.fine("Attempt to change password for user: " + data.username);

        Transaction txn = datastore.newTransaction();

        try {
            Key userKey = datastore.newKeyFactory().setKind("User").newKey(data.username);
            Entity user = txn.get(userKey);

            if (user == null) {
                txn.rollback();
                return Response.status(Status.BAD_REQUEST).entity("User not found.").build();
            }

            String hashedPWD = (String) user.getString("user_pwd");
            if (!hashedPWD.equals(DigestUtils.sha512Hex(data.currentPassword))) {
                txn.rollback();
                return Response.status(Status.FORBIDDEN).entity("Incorrect current password.").build();
            }

            if (!data.newPassword.equals(data.confirmNewPassword)) {
                txn.rollback();
                return Response.status(Status.BAD_REQUEST).entity("New passwords do not match.").build();
            }

            // Update the user's password
            user = Entity.newBuilder(userKey)
                    .set("user_id", data.username)
                    .set("user_email", user.getString("user_email"))
                    .set("user_name", user.getString("user_name"))
                    .set("user_phone", user.getString("user_phone"))
                    .set("user_pwd", DigestUtils.sha512Hex(data.newPassword)) // Hash the new password
                    .set("user_profile", user.getString("user_profile"))
                    .set("user_occupation", user.getString("user_occupation"))
                    .set("user_workLocation", user.getString("user_workLocation"))
                    .set("user_household", user.getString("user_household"))
                    .set("user_cp", user.getString("user_cp"))
                    .set("user_nif", user.getString("user_nif"))
                    .set("user_photo", user.getString("user_photo"))
                    .set("user_role", user.getString("user_role"))
                    .set("user_state", user.getString("user_state"))
                    .build();

            txn.put(user);
            txn.commit();

            LOG.info("Password changed for user: " + data.username);
            return Response.ok("{}").build();

        } catch (Exception e) {
            LOG.severe("Error changing password: " + e.getMessage());
            if(txn.isActive()) {
                txn.rollback();
            }
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Internal Server Error.").build();
        } finally {
            if(txn.isActive()) {
                txn.rollback();
            }
        }
    }
    
    

}