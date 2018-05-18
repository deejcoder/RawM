package today.doingit.App;

import java.nio.channels.SocketChannel;
import java.util.ArrayList;

public class User {

    private String username;
    private SocketChannel key;

    private ArrayList<byte[]> messages = new ArrayList<byte[]>();

    public User(SocketChannel key) {
        this.key = key;
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
    protected SocketChannel getKey() {
        return key;
    }


}
