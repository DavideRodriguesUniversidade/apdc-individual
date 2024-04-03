package pt.unl.fct.di.apdc.firstwebapp.resources;

import java.util.logging.Logger;

import javax.ws.rs.Consumes;
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
    public Response removeUser(RegisterData data) {
        LOG.fine("Attempt to remove user: " + data.username);

        Transaction txn = datastore.newTransaction();

        try {
            Key userKey = datastore.newKeyFactory().setKind("User").newKey(data.username);
            txn.delete(userKey);
            
            txn.commit();

            LOG.info("User removed: " + data.username);
            return Response.ok("{}").build();
        } catch (Exception e) {
            LOG.severe("Error removing user: " + e.getMessage());
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
    @Path("/updateRole")
    public Response updateUserRole(RegisterData data) {
        LOG.fine("Attempt to update role for user: " + data.username);

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
    public Response updateUserState(RegisterData data) {
        LOG.fine("Attempt to update state for user: " + data.username);

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
    public Response updateUser(RegisterData data) {
        LOG.fine("Attempt to update user: " + data.username);

        Transaction txn = datastore.newTransaction();

        try {
            Key userKey = datastore.newKeyFactory().setKind("User").newKey(data.username);
            Entity user = txn.get(userKey);
            
            if(user == null) {
                txn.rollback();
                return Response.status(Status.BAD_REQUEST).entity("User not found.").build();
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