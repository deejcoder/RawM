package today.doingit.App;

import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Iterator;

public class User {

    private String username;
    private SelectionKey key;
    private ArrayList<String> messages = new ArrayList<String>();
    private boolean authorized = false;

    /**
     * Constructor for creating a new user
     * @param key the key of the client
     */
    public User(SelectionKey key) {
        this.key = key;
    }


    /**
     * Is the current user authorized?
     * @return
     */
    public boolean isAuthorized() {
        return authorized;
    }

    /**
     * Set the user as authorized, or not authorized.
     * @param value
     */
    public void setAuthorized(boolean value) {
        authorized = value;
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
    public SelectionKey getKey() {
        return key;
    }

    /**
     * Sets the client's selection key
     * @param key
     */
    public void setKey(SelectionKey key) {
        this.key = key;
    }

    /**
     * Gets the user's socket channel
     * @return the channel belonging to the user
     */
    public SocketChannel getChannel() {
        return (SocketChannel) key.channel();
    }


    /**
     * Returns a list of all messages waiting to be sent
     * @return
     */
    public ArrayList<String> getMessages() {
        return messages;
    }

    /**
     * Adds a message to the message queue.
     * @param message
     * @return
     */
    public boolean queueMessage(String message) {
        messages.add(message);
        return true;
    }

    /**
     * Returns true if the user has any pending messages
     * @return
     */
    public boolean hasMessages() {
        if(messages.size() > 0) {
            return true;
        }
        return false;
    }

    /**
     * A user is equal when they have the same username,
     * or it is the same object.
     * @param object the object being compared
     * @return
     *//*
    @Override
    public boolean equals(Object object) {
        if(this == object) return true;
        if(!(object instanceof User)) return false;

        User user = (User) object;


        if(this.username.equals(user.username)) {
            return true;
        }
        return false;
    }

*/
    /**
     * Contract: equals & hashcode must both be overridden for
     * HashMaps. The hashCode is based from the username.
     * @return the hash code
     *//*
    @Override
    public int hashCode() {
        return this.username.hashCode();
    }
*/
}
