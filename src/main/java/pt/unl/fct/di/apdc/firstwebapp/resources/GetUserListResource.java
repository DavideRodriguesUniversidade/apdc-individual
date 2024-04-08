package pt.unl.fct.di.apdc.firstwebapp.resources;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Query;
import com.google.cloud.datastore.QueryResults;
import com.google.cloud.datastore.StructuredQuery;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import pt.unl.fct.di.apdc.firstwebapp.util.AuthToken;
import pt.unl.fct.di.apdc.firstwebapp.util.AuthTokenManager;
import pt.unl.fct.di.apdc.firstwebapp.util.RegisterData;

@Path("/users")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class GetUserListResource {

    private static final Logger LOG = Logger.getLogger(GetUserListResource.class.getName());

    private final Gson g = new GsonBuilder().create();
    private final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();

    public GetUserListResource() {}

    @GET
    @Path("/list")
    public Response getUsersList() {
        LOG.fine("Attempt to retrieve users list.");

        List<RegisterData> userList = new ArrayList<>();

        Query<Entity> query = Query.newEntityQueryBuilder()
                .setKind("User")
                .build();

        QueryResults<Entity> results = datastore.run(query);

        results.forEachRemaining(entity -> {
            RegisterData userData = new RegisterData(
                entity.getString("user_id"),
                entity.getString("user_email"),
                entity.getString("user_name"),
                entity.getString("user_phone"),
                "",  // password
                entity.getString("user_profile"),
                entity.getString("user_occupation"),
                entity.getString("user_workLocation"),
                entity.getString("user_household"),
                entity.getString("user_cp"),
                entity.getString("user_nif"),
                entity.getString("user_photo"),
                entity.getString("user_role"),
                entity.getString("user_state")
            );
            userList.add(userData);
        });

        LOG.info("Retrieved " + userList.size() + " users.");

        return Response.ok(g.toJson(userList)).build();
    }
    
    @POST
    @Path("/check")
    public Response getUsersList(@HeaderParam("authToken") String authTokenID) {
        LOG.fine("Attempt to retrieve users list.");

        AuthToken token = AuthTokenManager.getToken(authTokenID);
        if (token == null) {
            LOG.warning("Invalid or expired token.");
            return Response.status(Status.UNAUTHORIZED).entity("Invalid or expired token.").build();
        }

        String userRoleFromToken = token.getRole();
        List<RegisterData> userList = new ArrayList<>();

        Query<Entity> query;
        if ("USER".equals(userRoleFromToken)) {
            query = Query.newEntityQueryBuilder()
                    .setKind("User")
                    .setFilter(StructuredQuery.PropertyFilter.eq("user_role", "USER"))
                    .setFilter(StructuredQuery.PropertyFilter.eq("user_profile", "PUBLIC"))
                    .setFilter(StructuredQuery.PropertyFilter.eq("user_state", "ACTIVE"))
                    .build();
        } else if ("GBO".equals(userRoleFromToken)) {
            query = Query.newEntityQueryBuilder()
                    .setKind("User")
                    .setFilter(StructuredQuery.PropertyFilter.eq("user_role", "USER"))
                    .build();
        } else if ("GA".equals(userRoleFromToken)) {
            query = Query.newEntityQueryBuilder()
                    .setKind("User")
                    .build();
        } else { // SU
            query = Query.newEntityQueryBuilder()
                    .setKind("User")
                    .build();
        }

        QueryResults<Entity> results = datastore.run(query);

        results.forEachRemaining(entity -> {
            RegisterData userData = new RegisterData(
                entity.getString("user_id"),
                entity.getString("user_email"),
                entity.getString("user_name"),
                entity.getString("user_phone"),
                "",  // password
                entity.getString("user_profile"),
                entity.getString("user_occupation"),
                entity.getString("user_workLocation"),
                entity.getString("user_household"),
                entity.getString("user_cp"),
                entity.getString("user_nif"),
                entity.getString("user_photo"),
                entity.getString("user_role"),
                entity.getString("user_state")
            );
            userList.add(userData);
        });

        LOG.info("Retrieved " + userList.size() + " users.");

        return Response.ok(g.toJson(userList)).build();
    }
}