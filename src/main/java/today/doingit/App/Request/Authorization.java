package today.doingit.App.Request;

//Google's JSON
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import today.doingit.App.User;
import today.doingit.Server.Server;

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
     * @param client the client sending the request
     * @param content the request content
     * @return the string to return to the client.
     */
    @RequestCallback(
            name = "authorization"
    )
    public static String OnIncomingRequest(Server server, SocketChannel client, String content) {
        /*
            Authorization request should be the following format
            {
                "username":"{USERNAME}",
                "password":"{PASSWORD}"
           }
         */

        //If already authorized, exit
        if(isAuthorized(client)) {
            return "A01: Already Authorized";
        }

        //Get the username & password sent from the client
        JsonParser parser = new JsonParser();
        JsonElement rootNode = parser.parse(content);

        if(rootNode.isJsonObject()) {
            JsonObject json = rootNode.getAsJsonObject();
            JsonElement username = json.get("username");
            JsonElement password = json.get("password");
            server.getClientList().put(new User(client), client);

            System.out.println("Sending authorization request using username=" + username + " and password=" + password);
            try {
                System.out.println("Authorization successful for client " + client.getRemoteAddress().toString() + " with username=" + username);
            }
            catch(IOException ie) {
                ie.printStackTrace();
                System.exit(0);
            }
        }
        return "{\"type\":\"broadcast\",\"body\":{\"type\":\"authorization\",\"body\":\"Authorized\"}}\r\n";
    }

    /**
     * Checks if the client has already been authorized
     * @param client the channel belonging to the client
     * @return true or false
     */
    public static boolean isAuthorized(SocketChannel client) {
        /*if(users.containsKey(client)) {
            return true;
        }*/
        return false;
    }
}
