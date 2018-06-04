package today.doingit.App.Request;

import com.google.gson.*;
import today.doingit.App.Database.Mongo;
import today.doingit.App.User;
import today.doingit.Server.ResponseHandler;
import today.doingit.Server.Server;


/**
 * The structure of a message request from a client
 */
final class MessageRequest {
    public String address;
    public String body;
}


/**
 * The structure of a message response from the server
 */
final class MessageResponse {
    public String sender;
    public String body;
}

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
    public static void OnIncomingMessage(Server server, Mongo mongo, User sender, String content) {


        try {

            //==> First deserialize the data into a MessageRequest object, using GSON
            GsonBuilder builder = new GsonBuilder();
            Gson gson = builder.create();
            MessageRequest request = gson.fromJson(content, MessageRequest.class);


            //==> Now, work on producing a response
            MessageResponse response = new MessageResponse();
            //Get the sender's username
            response.sender = sender.getUsername();
            //The message's body
            response.body = request.body;

            //Convert it to a string
            String responseString = gson.toJson(response);

            //Let's create a complex response, since it contains JSON inside the body.
            ResponseHandler.ComplexResponse(
                    server,
                    sender,
                    ResponseHandler.R_TYPE.BROADCAST,
                    "message",
                    responseString
            );

            System.out.println("Sending " + request.body + ", to " + request.address);
            return;

        }
        //It was a bad request, GSON couldn't deserialize
        catch(JsonParseException jpe) {
            jpe.printStackTrace();
        }

        ResponseHandler.BasicError(
                server,
                sender,
                "Bad Request"
        );
    }
}
