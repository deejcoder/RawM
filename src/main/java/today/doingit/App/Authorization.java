package today.doingit.App;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.nio.channels.SocketChannel;
import java.util.Map;

/**
 * Maintains a list of authenticated users
 */
public class Authorization {

    private static Map<SocketChannel, User> users;

    public Authorization(Map<SocketChannel, User> users) {
        this.users = users;
    }

    /**
     * OnIncomingRequest is invoked when there is an incoming authorization request.
     * @param client the client sending the request
     * @param content the request content
     * @return the string to return to the client.
     */
    @RequestCallback(
            name = "authorization"
    )
    private static String OnIncomingRequest(SocketChannel client, String content) {
        /*
            Authorization request should be the following format
            {
                "username":"{USERNAME}",
                "password":"{PASSWORD}"
           }
         */

        if(isAuthorized(client)) {
            return "A01: Already Authorized";
        }
        JsonParser parser = new JsonParser();
        JsonElement rootNode = parser.parse(content);

        if(rootNode.isJsonObject()) {
            JsonObject json = rootNode.getAsJsonObject();
            JsonElement username = json.get("username");
            JsonElement password = json.get("password");
        }
        return "test";
    }

    /**
     * Checks if the client has already been authorized
     * @param client the channel belonging to the client
     * @return true or false
     */
    public static boolean isAuthorized(SocketChannel client) {
        if(users.containsKey(client)) {
            return true;
        }
        return false;
    }
}
