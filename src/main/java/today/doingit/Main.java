/*
 *  TODO:
 *  - Implement proper logging using LogBack (https://logback.qos.ch/)
 *  - Handling when a remote host closes connection
 *  - Message processing & format using JSON
 *  |- Client -> Server -> Destination -> Server -> Client
 *      (Read, Write, Read (or not if no reply from dst), Write)
 */


package today.doingit;

import today.doingit.App.User;
import today.doingit.Server.Server;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map;

public class Main {

    //Stores users that have been authorized.
    private static Map<SocketChannel, User> clients = new HashMap<SocketChannel, User>();

    public static void main(String[] args) throws IOException {


	    Server server = new Server(5000, "127.0.0.1");



    }
}
