package today.doingit;

import com.google.gson.*;
import today.doingit.requests.*;

import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;



/*
    This is solely to avoid cluttering the Server class file.
    Mainly will be used for request handling inside the read method.

    This file needs a bit more cleaning
 */
public class RequestHandler {

    /*
        CLIENT INFORMATION
     */
    //This is a HashMap which allows messages waiting to be sent to be queued.

    private static Map<SocketChannel, User> users = new HashMap<SocketChannel, User>();
    private static Map<String, User> reverseusers = new HashMap<String, User>();
    private static Map<String, Group> groups = new HashMap<String, Group>();


    //TO BE CONVERTED TO JSON
    public static void processMessage(SelectionKey key, String content) {

        SocketChannel channel = (SocketChannel)key.channel();
        User user = users.get(channel);

        JsonParser parser = new JsonParser();
        JsonElement rootNode = parser.parse(content);
        if(rootNode.isJsonObject()) {
            JsonObject json = rootNode.getAsJsonObject();
            JsonElement typeNode = json.get("type");


            switch(typeNode.getAsString()) {
                case "authorize": {
                    JsonElement authorizeNode = json.get("authorize");
                    authorizeUser(key, authorizeNode.getAsString());
                    break;
                }
                case "message": {
                    JsonElement messageNode = json.get("message");
                    if(messageNode.isJsonObject()) {

                        //To be moved... & called upon
                        GsonBuilder builder = new GsonBuilder();
                        builder.registerTypeAdapter(Message.class, new MessageAdapter());
                        Gson gson = builder.create();

                        Message message;
                        try {
                            message = gson.fromJson(messageNode, Message.class);
                        }
                        catch(InvalidMessageException ime) {
                            ime.printStackTrace();
                            return; //invalid format
                        }
                        System.out.println(message.getUsername());

                        //This is here temp. for testing
                        if(reverseusers.containsKey(message.getUsername())) {
                            User sendto = reverseusers.get(message.getUsername());
                            sendto.sendMessage(user.getUsername() + " says: " + message.getMessage());
                        }
                    }
                    break;
                }
            }
        }
    }

    //Will decide about this later
    protected static ArrayList<byte[]> getMessageQueue(SocketChannel channel) {
        User user = users.get(channel);
        return user.getMessageQueue();
    }

    protected static void authorizeUser(SelectionKey key, String nick) {
        //Try create a new user & set username
        User user = new User(key);
        if(user.setUsername(nick)) {

            //If username OK, add new User to users
            users.put((SocketChannel)key.channel(), user);
            reverseusers.put(user.getUsername(), user);
            user.sendMessage("Welcome, " + nick);
        }
        else {
            //Close connection... (ADD LATER)
            return;
        }
    }
}
