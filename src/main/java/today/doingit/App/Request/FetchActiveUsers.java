package today.doingit.App.Request;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import today.doingit.App.Database.Mongo;
import today.doingit.App.User;
import today.doingit.Server.ResponseHandler;
import today.doingit.Server.Server;

import java.util.ArrayList;

/**
 * A request to get a list of all active users on the server.
 */
public class FetchActiveUsers {

    public FetchActiveUsers() {}

    @RequestCallback(
            name="FetchActiveUsers"
    )
    public static void OnIncomingMessage(Server server, Mongo mongo, User sender, String data) {

        //Stores all the usernames to a list. TODO: enable serialization of User objects
        ArrayList<String> userlist = new ArrayList<String>();
        for(User user : server.getClientList()) {

            if(user.getUsername() != null) {
                userlist.add(user.getUsername());
            }
        }

        //Convert the list into JSON
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        String body = gson.toJson(userlist);


        //Complex response, since JSON in the body.
        ResponseHandler.ComplexResponse(
                server,
                sender,
                ResponseHandler.R_TYPE.USER,
                "activeusers",
                body
        );

    }
}
