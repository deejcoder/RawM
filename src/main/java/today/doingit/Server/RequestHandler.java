package today.doingit.Server;

import com.google.gson.*;
import today.doingit.App.Group;
import today.doingit.App.User;
import today.doingit.requests.*;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
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


}
