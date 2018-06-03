/*
    <h2>RawMessenger</h2>
    A messenger application offering high privacy to its users,
    with full control over their data & messages.

    @author Dylan Tonks
    @version 1.0
    @since 2018-03-31
 */

package today.doingit.Server;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import today.doingit.App.Database.Mongo;
import today.doingit.App.User;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.*;

public class Server {

    /*
        INITIAL CONSTANTS/VARS
     */
    private SocketAddress addr;
    private ServerSocketChannel server;
    private Selector selector;

    //This handles incoming requests.
    private RequestHandler requestHandler;

    private final static int bufferSize = 512;

    //Stores users that have been authorized.
    private static BiMap<User, SelectionKey> clients = HashBiMap.create();

    //The database
    private Mongo mongo;




    /**
     * Initializes a non-blocking asynchronous Server object.
     * Binds a new Socket to a provided address and begins listening on a new thread.
     * @param port Start the server on this port.
     * @param ip Start the server on this IP.
     * @throws IOException
     */
    public Server(int port, String ip, Mongo mongo) throws IOException {

        if(selector != null) return;
        if(server != null) return;

        //Setup & bind on address (ip, port)
        selector = Selector.open();
        server = ServerSocketChannel.open();
        server.configureBlocking(false);


        //Begin listening for incoming connections
        addr = new InetSocketAddress(ip, port);
        server.socket().bind(addr);

        //Create a new request handler
        requestHandler = new RequestHandler(mongo);

        //Set up the selection key
        server.register(selector, SelectionKey.OP_ACCEPT);

        System.out.println("Server has been opened at " + ip + ":" + port);

        //Start the server in another thread.
        run();

    }

    public void run() {

        while (true) {
            try {
                selector.select();
            }
            //Client has closed connection
            catch(IOException ex) {
                ex.printStackTrace();
                return;
            }

            //Maintains a request for every channel
            Set<SelectionKey> selectedKeys = selector.selectedKeys();
            Iterator<SelectionKey> iter = selectedKeys.iterator();

            while (iter.hasNext()) {
                SelectionKey key = iter.next();

                if(!key.isValid()) {
                    continue;
                }

                if (key.isAcceptable()) {
                    accept(selector);

                } else if (key.isWritable()) {
                    write(key);

                } else if (key.isReadable()) {
                    read(key);
                }

                //Remove the request from the Set
                iter.remove();
            }
        }
    }


    /*
     * Accepts a new client connection and creates a new socket
     * for the client. Then, tells the server to read from the client.
     */
    private void accept(Selector selector) {
        try {

            SocketChannel client = server.accept();
            //Disable blocking on new client channel
            client.configureBlocking(false);
            //Tell the server to wait & read from the client
            SelectionKey key = client.register(selector, SelectionKey.OP_READ);
            addUser(key);

        }
        catch(IOException ex) {
            ex.printStackTrace();
        }
    }

    private void read(SelectionKey key) {
        User user = getUser(key);
        SocketChannel client = user.getChannel();

        try {

            //Read from the socket
            ByteBuffer buffer = ByteBuffer.allocate(bufferSize);
            int read = client.read(buffer);

            //If there was anything in the read buffer...
            if(read > 0) {

                String data = new String(buffer.array(), "UTF-8");

                //Prune null values from the string.
                data = data.replaceAll("\u0000.*,", "");

                System.out.println(data);

                String response = requestHandler.handleRequest(this, user, data);
                ResponseHandler.handleResponse(this, user, response);

                //Write to the client if there are any messages to be sent
                if(user.hasMessages()) {
                    key.interestOps(SelectionKey.OP_WRITE);
                }
            }
        }
        catch(IOException ex) {
            ex.printStackTrace();
            key.cancel();
        }
    }


    private void write(SelectionKey key) {

        User user = getUser(key);
        SocketChannel client = user.getChannel();

        try {

            //Get next message in queue belonging to THIS client channel & remove it
            ArrayList<String> messages = user.getMessages();
            while(!messages.isEmpty()) {
                String message = messages.remove(0);
                System.out.println("Sending to client: " + message);
                client.write(ByteBuffer.wrap(message.getBytes()));
            }

            key.interestOps(SelectionKey.OP_READ);
        }
        catch(IOException ex) {
            ex.printStackTrace();
        }
        //For now NullPointerException would be thrown if the server doesn't write back to the client.
        catch(NullPointerException ex) {
            ex.printStackTrace();
            key.interestOps(SelectionKey.OP_READ);
        }
    }

    /*
        UTILS
        ====
     */
    /**
     * Add a message to the message queue that will be sent next time
     * the server is writing to the client.
     * @param key the client that should be sent the message.
     * @param message the actual message.
     */
    public void pushMessage(SelectionKey key, String message) {
        User user = getUser(key);
        user.queueMessage(message);
    }

    /**
     * Adds a message to the message queue and tells the server to send the queue
     * to the client, given the username.
     * @param user the username to send the queue.
     * @param message the actual message.
     * @return true if the message was sent, otherwise false (invalid user was provided)
     */
    public boolean send(User user, String message) {
        SelectionKey key = user.getKey();
        pushMessage(key, message);
        if(key.isValid()) {
            key.interestOps(SelectionKey.OP_WRITE);
            return true;
        }
        return false;
    }


    /**
     * Returns a list of the currently connected users.
     * @return Set<String>
     */
    public Set<User> getClientList() {
        return clients.keySet();
    }

    /**
     * Adds a new client to the connected users list.
     * @param username the username of the client to add.
     * @param key the key belonging to the client.
     */
    public User addUser(String username, SelectionKey key) {
        User user = new User(key);
        user.setUsername(username);

        clients.put(user, key);
        return user;
    }

    /**
     * Adds a new user when the username is unknown
     * @param key
     */
    public User addUser(SelectionKey key) {
        User user = new User(key);
        clients.put(user, key);
        return user;
    }

    /**
     * Returns the username of a client, given their key.
     * @param key the key of the client.
     * @return the username as a String.
     */
    public User getUser(SelectionKey key) {
        if(clients.inverse().containsKey(key)) {
            return clients.inverse().get(key);
        }
        return addUser(key);
    }

}
