package today.doingit.App.Request;


import today.doingit.App.User;
import today.doingit.Server.Server;

public interface RequestInterface {
    void OnIncomingRequest(Server server, User sender, String content);
}