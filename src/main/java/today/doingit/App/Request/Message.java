package today.doingit.App.Request;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.nio.channels.SocketChannel;

public class Message {
    public Message() {}


    /**
     * Handles an incoming message request. This invokes the {@link RequestCallback} annotation.
     * @param client
     * @param content
     * @return
     */
    @RequestCallback(
            name="message"
    )
    public static String OnIncomingMessage(SocketChannel client, String content) {

        /*
            A message is structured,
            {
                "address":"JohnDoe",
                "body":"The message body"
            }
         */
        //Get the message content
        JsonParser parser = new JsonParser();
        JsonElement rootNode = parser.parse(content);

        if(rootNode.isJsonObject()) {

            JsonObject json = rootNode.getAsJsonObject();
            JsonElement address = json.get("address");
            JsonElement body = json.get("body");

            //Do stuff
            System.out.println("Sending " + body.toString() + ", to " + address);
            return body.toString();

        }


        return "Invalid request";
    }
}
