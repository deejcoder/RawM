package today.doingit.App.Request;

//Google's JSON
import com.google.gson.*;
import today.doingit.Server.Server;

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
    public static String OnIncomingRequest(Server server, SelectionKey client, String content) {
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
                    return "{\"type\":\"error\", \"body\":\"The username is already taken!\"}\r\n";
                }

                //The user is now AUTHORIZED, add them to the client list.
                server.addClient(username, client);



                /*
                    To be transformed into LogBack framework
                    >===
                 */
                SocketChannel clientChannel = (SocketChannel) client.channel();

                System.out.println("Sending authorization request using username=" + username + " and password=" + password);
                try {
                    System.out.println("Authorization successful for client " + clientChannel.getRemoteAddress().toString() + " with username=" + username);
                } catch (IOException ie) {
                    ie.printStackTrace();
                    return "";
                }
                //===<
            }
            return "{\"type\":\"broadcast\",\"body\":{\"type\":\"authorization\",\"body\":\"Authorized\"}}\r\n";
        }
        catch(JsonParseException jpe) {
            jpe.printStackTrace();
        }
        return "{\"type\":\"error\", \"body\":\"500: Bad Request\"}\r\n";

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
