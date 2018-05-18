/*
    <h2>RawMessenger</h2>
    A messenger application offering high privacy to its users,
    with full control over their data & messages.

    @author Dylan Tonks
    @version 1.0
    @since 2018-03-31
 */

package today.doingit.Server;

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
    private RequestHandler requestHandler;

    private final static int bufferSize = 512;



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

        //Create a new request handler
        requestHandler = new RequestHandler();

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
                System.out.println(data);

                requestHandler.handleRequest(client, data.replaceAll("\u0000.*,", ""));
                //RequestHandler.processMessage(key, data.replaceAll("\u0000.*", ""));
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

        //try {

            //Get next message in queue belonging to THIS client channel & remove it
            //ArrayList<byte[]> messages = RequestHandler.getMessageQueue(client);

            /*while(!messages.isEmpty()) {
                byte[] data = messages.remove(0);
                client.write(ByteBuffer.wrap(data));
            }*/

            key.interestOps(SelectionKey.OP_READ);
       // }
       // catch(IOException ie) {
        //    ie.printStackTrace();
        //}
    }

}
