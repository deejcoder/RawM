package today.doingit.requests;

import today.doingit.Group;
import today.doingit.User;

public class Message {

    private String username;
    private String group;
    private String message;


    /*
        Getters
     */
    public String getUsername() { return username; }
    public String getGroup() { return group; }
    public String getMessage() { return message; }

    public void setUsername(String username) {
        this.username = username;
    }


    /*
        Setters
     */
    public void setGroup(String group) {
        this.group = group;

    }

    public void setMessage(String message) {
        this.message = message;
    }



}
