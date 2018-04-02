package today.doingit;

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
public class MessageHandler {

    /*
        CLIENT INFORMATION
     */
    //This is a HashMap which allows messages waiting to be sent to be queued.

    private static Map<SocketChannel, User> users = new HashMap<SocketChannel, User>();
    private static Map<String, Group> groups = new HashMap<String, Group>();


    //TO BE CONVERTED TO JSON
    public static void processMessage(SelectionKey key, String data) {

        SocketChannel channel = (SocketChannel)key.channel();
        /*
            Client must first IDENTIFY themselves
            -----
         */
        User user = users.get(channel);

        if(data.startsWith("Nick:") && !users.containsKey(channel)) {

            //Get client's nick & add to HashMap, nicks
            //Will introduce proper system for parameter values later
            String nick = data.substring(5).trim();
            authorizeUser(key, nick);
        }

        //Just testing groups for now...
        else if(data.startsWith("CreateGroup:")) {

            String gname = data.substring(12).trim();
            Group group = new Group(gname);
            groups.put(gname, group);
            group.addMember(user);

        }
        //For now, Group:[groupName]:[groupMsg]
        else if(data.startsWith("Group:")) {
            String[] params = data.split(":");
            System.out.println(params);
            Group group = groups.get(params[1]);
            group.sendMessage("(" + params[1] + ") " + user.getUsername() + " says: " + params[2]);

        }
        else if(data.startsWith("JoinGroup:")) {

            String gname = data.substring(10).trim();
            System.out.println(gname);
            Group group = groups.get(gname);
            group.addMember(user);
        }
        else {

            System.out.println(user.getUsername() + " says: " + data + "\n");

                    /*
                        For now, send a message to all users connected...
                        ------
                     */
            for (User otheruser : users.values()) {
                try {
                    otheruser.sendMessage(user.getUsername() + " says: " + data);
                } catch (Exception e) {
                    //Chances are, it's a ServerSocketChannel, so ignore it
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
            user.sendMessage("Welcome, " + nick);
        }
        else {
            //Close connection... (ADD LATER)
            return;
        }
    }
}
