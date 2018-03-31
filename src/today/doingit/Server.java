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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

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
    private Map<SocketChannel, byte[]> messages = new HashMap<SocketChannel, byte[]>();


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

            //Maintains a list of requests
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

    private void read(SelectionKey key) {
        SocketChannel client = (SocketChannel) key.channel(); //get current client

        try {

            //Read from the socket
            ByteBuffer buffer = ByteBuffer.allocate(bufferSize);
            int read = client.read(buffer);

            //If there was anything in the read buffer...
            if(read > 0) {
                String data = new String(buffer.array(), "UTF-8");
                System.out.println("From: " + client.getRemoteAddress() + "\nMessage: " + data);

                push(key.channel(), "From: " + client.getLocalAddress() + "\nMessage: " + data + "\r\n");
                key.interestOps(SelectionKey.OP_WRITE);
            }
        }
        catch(IOException ie) {
            ie.printStackTrace();
        }
    }

    /**
     * Adds a message to send to a client's channel to the queue.
     * @param selectableChannel The client channel to send the message to.
     * @param message The message to send to the client channel.
     * @see SelectableChannel
     */
    public void push(SelectableChannel selectableChannel, String message) {
        byte[] data = message.getBytes();
        messages.put((SocketChannel)selectableChannel, data);
    }


    /*
        Takes an item from the message queue belonging to client channel x,
        and writes.
     */
    private void write(SelectionKey key) {
        //Get client channel
        SocketChannel client = (SocketChannel) key.channel();

        try {

            //Get next message in queue belonging to THIS client channel & remove it
            byte[] data = messages.get(client);
            messages.remove(client);

            client.write(ByteBuffer.wrap(data));
            key.interestOps(SelectionKey.OP_READ);
        }
        catch(IOException ie) {
            ie.printStackTrace();
        }
    }
}
