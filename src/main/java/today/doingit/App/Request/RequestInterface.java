package today.doingit.App.Request;


import today.doingit.App.Database.Mongo;
import today.doingit.App.User;
import today.doingit.Server.Server;

public interface Request {
    void OnIncomingRequest(Server server, Mongo mongo, User sender, String content);
}