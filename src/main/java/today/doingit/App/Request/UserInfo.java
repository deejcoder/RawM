package today.doingit.App.Request;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import today.doingit.App.User;
import today.doingit.Server.ResponseHandler;
import today.doingit.Server.Server;


final class UserInfoRequest {
    public String username;
}

@RequestCallback(
        name = "userinfo"
)
public class UserInfo extends Request {

    @Override
    public void OnIncomingRequest(Server server, User sender, String content) {

        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        UserInfoRequest request = gson.fromJson(content, UserInfoRequest.class);

        if(!server.mongo.validUser(request.username)) {
            ResponseHandler.BasicError(server, sender, "The user does not exist.");
            return;
        }

        //TODO: get user details: description etc from mongo, send as json to client.
        String info = server.mongo.getUserInfo(request.username);
        System.out.println(info);
        ResponseHandler.BasicResponse(
                server,
                sender,
                ResponseHandler.R_TYPE.USER,
                "userinfo",
                info
        );

    }
}
