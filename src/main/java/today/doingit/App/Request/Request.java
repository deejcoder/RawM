package today.doingit.App.Request;

import today.doingit.App.Database.Mongo;
import today.doingit.App.User;
import today.doingit.Server.Server;

public class RequestType implements Request {
    @Override
    public void OnIncomingRequest(Server server, Mongo mongo, User sender, String content) {

    }
}
