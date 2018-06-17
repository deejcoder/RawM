package today.doingit.Server;

import java.util.HashMap;
import java.util.IllegalFormatConversionException;
import java.util.Set;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mongodb.util.JSONParseException;
import org.reflections.Reflections;
import java.lang.annotation.Annotation;

import today.doingit.App.Database.Mongo;
import today.doingit.App.Request.Request;
import today.doingit.App.Request.RequestCallback;
import today.doingit.App.User;


public class RequestHandler {


    //Map of all callbacks in request type classes
    private final static HashMap<String, Class<?>> requestCallbacks = new HashMap<>();

    //the db
    private Mongo mongo;

    /**
     * Constructor which when initialized will examine the classpath and generate a HashMap
     * containing all methods inside classes within the package, today.doingit.App that have the
     * {@link RequestCallback} annotation.
     */
    public RequestHandler(Mongo mongo) {

        this.mongo = mongo;

        /*
            This uses Reflections to examine the classpath
         */
        Reflections reflections = new Reflections("today.doingit.App");

        //Gets all classes within the URI, today.doingit.App, that are annotated with @RequestCallback
        System.out.println("Server started with the following request types:");
        Set<Class<?>> annotated = reflections.getTypesAnnotatedWith(RequestCallback.class);
        for(Class<?> clazz : annotated) {
            Annotation ann = clazz.getAnnotation(RequestCallback.class);

            //Get the request's name
            RequestCallback callback = (RequestCallback) ann;

            //Store them by request name
            requestCallbacks.put(callback.name(), clazz);
            System.out.println(callback.name());
        }
    }

    /**
     * Handles a request, given the client and content passed from the client.
     * @param user the User
     * @param content the content sent by the client
     * @return true if the request was valid and handled, else false.
     */
    public void handleRequest(Server server, User user, String content) {

        //Get the request TYPE & request MESSAGE
        try {

            JsonParser parser = new JsonParser();
            JsonObject obj = parser.parse(content.trim()).getAsJsonObject();

            String type = obj.get("type").getAsString();
            String message = obj.get("message").toString();


            //It is considered a valid request if the request type exists in RequestCallbacks
            if (isValidRequest(type)) {

                //Invoke the request type's callback. i.e if type = authorization, invoke OnMessageRequest in Authorization
                try {

                    Object object  = requestCallbacks.get(type).newInstance();
                    Request request = (Request) object;

                    request.OnIncomingRequest(server, user, message);
                    return;

                } catch (IllegalAccessException | IllegalFormatConversionException | InstantiationException | ClassCastException ex) {
                    ex.printStackTrace();
                    ResponseHandler.BasicError(server, user, "Bad Request");
                    return;
                }
            }
            //Return Bad Request if the request type doesn't exist
        }
        //Bad JSON Request
        catch(JSONParseException ex) {
            ex.printStackTrace();
        }
        ResponseHandler.BasicError(server, user, "Bad Request");
    }

    /**
     * Checks if the provided request type is a valid request type.
     * @param requestName a String representing the request type.
     * @return true if the request is valid, else false.
     */
    public boolean isValidRequest(String requestName) {
        return (requestCallbacks.containsKey(requestName));

    }
}
