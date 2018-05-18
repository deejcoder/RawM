package today.doingit.App.Request;

import java.nio.channels.SocketChannel;

public class Message {
    public Message() {}

    @RequestCallback(
            name="message"
    )
    public static String OnIncomingMessage(SocketChannel client, String content) {
        System.out.println("Message");
        return "Message";
    }
}
