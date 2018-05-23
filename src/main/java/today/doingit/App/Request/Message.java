package today.doingit.App.Request;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import today.doingit.App.Database.Mongo;
import today.doingit.Server.Server;

import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

public class Message {
    public Message() {}


    /**
     * Handles an incoming message request. This invokes the {@link RequestCallback} annotation.
     * @param server the server that was given the incoming request.
     * @param key the key of the client who sent the request.
     * @param content the request content.
     * @return a String containing the response to be sent to the client.
     */
    @RequestCallback(
            name="message"
    )
    public static String OnIncomingMessage(Server server, Mongo mongo, SelectionKey key, String content) {

        SocketChannel client = (SocketChannel) key.channel();

        /*
            A message is structured,
            {
                "address":"JohnDoe",
                "body":"The message body"
            }
         */
        //Get the message content
        try {
            JsonParser parser = new JsonParser();
            JsonElement rootNode = parser.parse(content);

            if (rootNode.isJsonObject()) {

                JsonObject json = rootNode.getAsJsonObject();
                JsonElement address = json.get("address");
                String body = json.get("body").getAsString();

                //Annotate the message with the sender's username.
                body = server.getClientUsername(key) + " says: " + body;

                System.out.println("Sending " + body + ", to " + address);
                return "{\"type\":\"broadcast\",\"body\":{\"type\":\"message\",\"body\":\"" + body + "\"}}\r\n";

            }
        }
        catch(JsonParseException jpe) {
            jpe.printStackTrace();
        }

        //TODO: change to error
        return "Invalid request";
    }
}
