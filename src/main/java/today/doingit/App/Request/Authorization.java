package today.doingit.App.Request;

//Google's JSON
import com.google.gson.*;
import today.doingit.App.Database.Mongo;
import today.doingit.Server.Server;
import today.doingit.App.Response.ResponseUtil;

//Exceptions
import java.io.IOException;

//Networking
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.Set;

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
     * @param client the client sending the request
     * @param content the request content
     * @return the string to return to the client.
     */
    @RequestCallback(
            name = "authorization"
    )
    public static String OnIncomingRequest(Server server, Mongo mongo, SelectionKey client, String content) {
        /*
            Authorization request should be the following format
            {
                "username":"{USERNAME}",
                "password":"{PASSWORD}"
           }
         */

        //Try to parse the JSON
        try {

            //Get the username & password sent from the client
            JsonParser parser = new JsonParser();
            JsonElement rootNode = parser.parse(content);

            if (rootNode.isJsonObject()) {
                JsonObject json = rootNode.getAsJsonObject();
                String username = json.get("username").getAsString();
                JsonElement password = json.get("password");

                //If already authorized, exit
                if(isAuthorized(server, username)) {

                    //Will write a proper error class later.
                    return ResponseUtil.error("The username is already taken");
                }

                if(!mongo.validUser(username)) {
                    return ResponseUtil.error("The user does not exist");
                }

                //The user is now AUTHORIZED, add them to the client list.
                server.addClient(username, client);

                /*
                    To be transformed into LogBack framework
                    >===
                 */
                SocketChannel clientChannel = (SocketChannel) client.channel();

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

    /**
     * Checks if someone by the given username is already connected.
     * @param server the server the client is connected to
     * @param username the username the client desires.
     * @return true or false
     */
    public static boolean isAuthorized(Server server, String username) {
        Set<String> clients = server.getClientList();
        if(clients.contains(username)) {
            return true;
        }
        return false;
    }
}
