/*
    <h2>RawMessenger</h2>
    A messenger application offering high privacy to its users,
    with full control over their data & messages.

    @author Dylan Tonks
    @version 1.0
    @since 2018-03-31
 */

package today.doingit;

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

    private final static int bufferSize = 512;


    /*
        CLIENT INFORMATION
     */
    //This is a HashMap which allows messages waiting to be sent to be queued.

    /*
        NOTE: ALL THESE VARIABLES WILL BE MOVED TO RequestHandler
     */
    private Map<SocketChannel, User> users = new HashMap<SocketChannel, User>();
    private Map<String, Group> groups = new HashMap<String, Group>();


    /**
     * Initializes a non-blocking asynchronous Server object.
     * Binds a new Socket to a provided address and begins listening on a new thread.
     * @param port Start the server on this port.
     * @param ip Start the server on this IP.
     * @throws IOException
     */
    public Server(int port, String ip) throws IOException {

        if(selector != null) return;
        if(server != null) return;

        //Setup & bind on address (ip, port)
        selector = Selector.open();
        server = ServerSocketChannel.open();
        server.configureBlocking(false);

        //Begin listening for incoming connections
        addr = new InetSocketAddress(ip, port);
        server.socket().bind(addr);

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
            catch(IOException ie) {
                ie.printStackTrace();
                break;
            }

            //Maintains a request for every channel
            Set<SelectionKey> selectedKeys = selector.selectedKeys();
            Iterator<SelectionKey> iter = selectedKeys.iterator();

            while (iter.hasNext()) {
                SelectionKey key = iter.next();

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

    /*
        This entire section needs cleaning
        NOTE: majority of this will go to RequestHandler
     */
    private void read(SelectionKey key) {
        SocketChannel client = (SocketChannel) key.channel(); //get current client

        try {

            //Read from the socket
            ByteBuffer buffer = ByteBuffer.allocate(bufferSize);
            int read = client.read(buffer);

            //If there was anything in the read buffer...
            if(read > 0) {

                String data = new String(buffer.array(), "UTF-8");

                /*
                    Client must first IDENTIFY themselves
                    -----
                 */
                User user = users.get(client);

                if(data.startsWith("Nick:") && !users.containsKey(client)) {

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
                            otheruser.sendMessage(user.getUsername() + " says: " + data + "\r");
                        } catch (Exception e) {
                            //Chances are, it's a ServerSocketChannel, so ignore it
                        }
                    }
                }
            }
        }
        catch(IOException ie) {
            ie.printStackTrace();
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
        User user = users.get(client);

        try {

            //Get next message in queue belonging to THIS client channel & remove it
            ArrayList<byte[]> messages = user.getMessageQueue();

            while(!messages.isEmpty()) {
                byte[] data = messages.remove(0);
                client.write(ByteBuffer.wrap(data));
            }

            key.interestOps(SelectionKey.OP_READ);
        }
        catch(IOException ie) {
            ie.printStackTrace();
        }
    }

    //To: RequestHandler
    protected void authorizeUser(SelectionKey key, String nick) {
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
