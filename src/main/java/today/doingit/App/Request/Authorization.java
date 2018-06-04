package today.doingit.App.Request;

//Google's JSON
import com.google.gson.*;
import com.mongodb.util.JSONParseException;
import today.doingit.App.Database.Mongo;
import today.doingit.App.User;
import today.doingit.Server.ResponseHandler;
import today.doingit.Server.Server;

/**
 * This class handles everything to do with user
 * authorization. This is simply a request type.
 *
 * TODO: Maintain connection to database, finish proper authorization.
 */
final class AuthorizationRequest {
    public String username;
    public String password;

}

public class Authorization extends Request {

    public Authorization() {
    }

    /**
     * OnIncomingRequest is invoked when there is an incoming authorization request. This is
     * declared by the {@link RequestCallback} annotation.
     * @param sender the user sending the request
     * @param content the request content
     * @return the string to return to the client.
     */
    @RequestCallback(
            name = "authorization"
    )
    public static void OnIncomingRequest(Server server, Mongo mongo, User sender, String content) {


        //If already authorized, exit
        if(sender.isAuthorized()) {

            //Will write a proper error class later.
            ResponseHandler.BasicError(server, sender, "The user is already logged in.");
            return;
        }


        //Try to parse the JSON
        try {


            GsonBuilder builder = new GsonBuilder();
            Gson gson = builder.create();
            AuthorizationRequest request = gson.fromJson(content, AuthorizationRequest.class);


            //Check if the user exists in the database TODO: password checks & encryption
            if(!mongo.validUser(request.username)) {
                ResponseHandler.BasicError(server, sender, "The user does not exist.");
                return;
            }

            //The user is now AUTHORIZED, add them to the client list.
            sender.setUsername(request.username);
            sender.setAuthorized(true);


            //Basic response, telling the client they're authorized.
            //TODO: send back user data instead
            ResponseHandler.BasicResponse(
                    server,
                    sender,
                    ResponseHandler.R_TYPE.USER,
                    "authorization",
                    "Authorized"
            );

            FetchActiveUsers.broadcastUserList(server, sender);

           return;
        }
        catch(JSONParseException ex) {
            ex.printStackTrace();
        }

        ResponseHandler.BasicError(server, sender, "Bad Request");
        return;

    }
}
