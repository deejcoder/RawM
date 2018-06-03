package today.doingit.App.Request;

//Google's JSON
import com.google.gson.*;
import today.doingit.App.Database.Mongo;
import today.doingit.App.User;
import today.doingit.Server.Server;
import today.doingit.App.Response.ResponseUtil;

//Exceptions
import java.io.IOException;

//Networking
import java.nio.channels.SocketChannel;

/**
 * This class handles everything to do with user
 * authorization. This is simply a request type.
 *
 * TODO: Maintain connection to database, finish proper authorization.
 */
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
    public static String OnIncomingRequest(Server server, Mongo mongo, User sender, String content) {

        /*
            Authorization request should be the following format
            {
                "username":"{USERNAME}",
                "password":"{PASSWORD}"
           }
         */

        //If already authorized, exit
        if(sender.isAuthorized()) {

            //Will write a proper error class later.
            return ResponseUtil.error("The username is already taken");
        }


        //Try to parse the JSON
        try {

            //Get the username & password sent from the client
            JsonParser parser = new JsonParser();
            JsonElement rootNode = parser.parse(content);

            if (rootNode.isJsonObject()) {
                JsonObject json = rootNode.getAsJsonObject();
                String username = json.get("username").getAsString();
                JsonElement password = json.get("password");

                if(!mongo.validUser(username)) {
                    return ResponseUtil.error("The user does not exist");
                }

                //The user is now AUTHORIZED, add them to the client list.
                sender.setUsername(username);

                /*
                    To be transformed into LogBack framework
                    >===
                 */
                SocketChannel clientChannel = sender.getChannel();

                try {
                    System.out.println("Authorization successful for client " + clientChannel.getRemoteAddress().toString() + " with username=" + username);
                } catch (IOException ie) {
                    ie.printStackTrace();
                    return ResponseUtil.error("There was an unexpected error");
                }
                //===<
            }
            return ResponseUtil.generateResponse("client", "{\"type\":\"authorization\", \"body\":\"Authorized\"}"); //TODO
        }
        catch(JsonParseException jpe) {
            jpe.printStackTrace();
        }
        return ResponseUtil.error("Bad Request");

    }
}
