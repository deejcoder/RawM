/*
    <h2>RawMessenger</h2>
    A messenger application offering high privacy to its users,
    with full control over their data & messages.

    @author Dylan Tonks
    @version 1.0
    @since 2018-03-31
 */

package today.doingit.Server;

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

    //The message queue, for each user.
    private HashMap<SocketChannel, ArrayList<String>> messageQueue = new HashMap<SocketChannel, ArrayList<String>>();

    //Stores users that have been authorized.
    private static Map<String, SelectionKey> clients = new HashMap<String, SelectionKey>();
    private static Map<SelectionKey, String> reverseClients = new HashMap<SelectionKey, String>();

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
            catch(IOException ie) {
                ie.printStackTrace();
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
            client.register(selector, SelectionKey.OP_READ);
        }
        catch(IOException ie) {}
    }

    private void read(SelectionKey key) {
        SocketChannel client = (SocketChannel) key.channel(); //get current client

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

                String response = requestHandler.handleRequest(this, key, data);
                ResponseHandler.handleResponse(this, key, response);

                //Write to the client if there are any messages to be sent
                if(getMessageLength(client) > 0) {
                    key.interestOps(SelectionKey.OP_WRITE);
                }
            }
        }
        catch(IOException ie) {
            ie.printStackTrace();
            key.cancel();
        }
    }

    /*
        INTERNAL SERVER METHOD:
        write performs a N-I/O operation to the socket supplied with
        a list of messages waiting to be sent.
        This is because it invokes socket functions inside of this function.
     */
    private void write(SelectionKey key) {
        //Get client channel
        SocketChannel client = (SocketChannel) key.channel();

        try {

            //Get next message in queue belonging to THIS client channel & remove it

            ArrayList<String> messages = messageQueue.get(client);
            while(!messages.isEmpty()) {
                String data = messages.remove(0);
                System.out.println("Sending to client: " + data);
                client.write(ByteBuffer.wrap(data.getBytes()));
            }

            key.interestOps(SelectionKey.OP_READ);
        }
        catch(IOException ie) {
            ie.printStackTrace();
        }
        //For now NullPointerException would be thrown if the server doesn't write back to the client.
        catch(NullPointerException npe) {
            npe.printStackTrace();
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
     * @param client the client that should be sent the message.
     * @param message the actual message.
     */
    public void pushMessage(SocketChannel client, String message) {
        if(!messageQueue.containsKey(client)) {
            messageQueue.put(client, new ArrayList<String>());
        }

        messageQueue.get(client).add(message);
    }

    /**
     * Returns the number of messages currently waiting to be sent to the client.
     * @param client the client
     * @return the number of messages
     */
    public int getMessageLength(SocketChannel client) {
        if(!messageQueue.containsKey(client)) {
            return 0;
        }
        return messageQueue.get(client).size();
    }

    /**
     * Adds a message to the message queue and tells the server to send the queue
     * to the client, given the username.
     * @param username the username to send the queue.
     * @param message the actual message.
     * @return true if the message was sent, otherwise false (invalid user was provided)
     */
    public boolean send(String username, String message) {

        //Check if the username is used by an existing client
        if(clients.containsKey(username)) {

            SelectionKey key = clients.get(username);

            return send(key, message);
        }
        return false;
    }

    /**
     * Adds a message to the message queue and tells the server to send it, given
     * the client's SelectionKey.
     * @param key the client's key.
     * @param message the message to add to the queue/send.
     * @return
     */
    public boolean send(SelectionKey key, String message) {
        if(key.isValid()) {
            SocketChannel client = (SocketChannel) key.channel();
            pushMessage(client, message);
            key.interestOps(SelectionKey.OP_WRITE);
            return true;
        }
        return false;
    }

    /**
     * Returns a list of the currently connected users.
     * @return Set<String>
     */
    public Set<String> getClientList() {
        return clients.keySet();
    }

    /**
     * Adds a new client to the connected users list.
     * @param username the username of the client to add.
     * @param key the key belonging to the client.
     */
    public void addClient(String username, SelectionKey key) {
        clients.put(username, key);
        reverseClients.put(key, username);
    }

    /**
     * Returns the username of a client, given their key.
     * @param key the key of the client.
     * @return the username as a String.
     */
    public String getClientUsername(SelectionKey key) {
        return reverseClients.get(key);
    }

}
