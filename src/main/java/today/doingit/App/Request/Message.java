package today.doingit.App.Request;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import today.doingit.App.Database.Mongo;
import today.doingit.App.Response.ResponseUtil;
import today.doingit.App.User;
import today.doingit.Server.Server;

import java.nio.channels.SocketChannel;

public class Message {
    public Message() {}


    /**
     * Handles an incoming message request. This invokes the {@link RequestCallback} annotation.
     * @param server the server that was given the incoming request.
     * @param sender the key of the user who sent the request.
     * @param content the request content.
     * @return a String containing the response to be sent to the client.
     */
    @RequestCallback(
            name="message"
    )
    public static String OnIncomingMessage(Server server, Mongo mongo, User sender, String content) {

        SocketChannel client = sender.getChannel();

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

                System.out.println("Sending " + body + ", to " + address);
                //TODO: convert to JSON object
                return "{\"type\":\"broadcast\",\"body\":{\"type\":\"message\",\"sender\":\"" + sender.getUsername() + "\",\"body\":\"" + body + "\"}}\r\n";

            }
        }
        catch(JsonParseException jpe) {
            jpe.printStackTrace();
        }

        //TODO: change to error
        return ResponseUtil.error("Bad Request");
    }
}
