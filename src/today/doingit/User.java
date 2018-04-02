package today.doingit;

import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;

public class User {

    private String username;
    private SelectionKey key;

    private ArrayList<byte[]> messages = new ArrayList<byte[]>();

    public User(SelectionKey key) {
        this.key = key;
    }

    /**
     * Adds a message to the message queue which the server
     * will then send it to the client socket.
     * @param message A string containing the message to be sent to the user.
     */
    public void sendMessage(String message) {
        //All message formatting & verification will occur here.
        messages.add(message.getBytes());
        key.interestOps(SelectionKey.OP_WRITE);
    }

    protected ArrayList<byte[]> getMessageQueue() {
        return messages;
    }


    /**
     * Sets the user's username but first sees if its of valid format.
     * @param username The new username to set to the user.
     * @return
     */
    public boolean setUsername(String username) {
        if(username.matches("[A-Za-z0-9]{4,10}")) {
            this.username = username;
            return true;
        }
        return false;
    }

    /**
     * Get the user's current username.
     * @return User's username.
     */
    public String getUsername() {
        return username;
    }

    /**
     * Get the user's SelectionKey which can be used to get the user's channel.
     * @return SelectionKey key
     */
    protected SelectionKey getKey() {
        return key;
    }


}
