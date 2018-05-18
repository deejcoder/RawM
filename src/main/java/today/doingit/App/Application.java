package today.doingit.App;

import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map;
import today.doingit.App.Request.Authorization;

public class Application {

    //Stores users that have been authorized.
    private static Map<SocketChannel, User> users = new HashMap<SocketChannel, User>();

    //
    private Authorization auth;

    public Application() {

        //Manages and maintains authorization requests
        auth = new Authorization();

    }
}
