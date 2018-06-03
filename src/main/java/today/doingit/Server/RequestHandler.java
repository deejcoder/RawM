package today.doingit.Server;


//Data structures
import java.util.HashMap;
import java.util.IllegalFormatConversionException;
import java.util.Set;

//Google's JSON
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

//Reflections/Annotation
import com.mongodb.util.JSONParseException;
import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

//Internal
import today.doingit.App.Database.Mongo;
import today.doingit.App.Request.RequestCallback;
import today.doingit.App.Response.ResponseUtil;
import today.doingit.App.User;


public class RequestHandler {


    //Map of all callbacks in request type classes
    private final static HashMap<String, Method> requestCallbacks = new HashMap<String, Method>();

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
        Reflections reflections = new Reflections(
            new ConfigurationBuilder()
                .setUrls(ClasspathHelper.forPackage("today.doingit.App"))
                .setScanners(new MethodAnnotationsScanner())
        );

        //Gets all methods within the URI, today.doingit.App, that are annotated with @RequestCallback
        Set<Method> annotated = reflections.getMethodsAnnotatedWith(RequestCallback.class);

        /*
            Loop through all the methods which contain the annotation
         */
        for(Method method : annotated) {
            if (method.isAnnotationPresent(RequestCallback.class)) {

                Annotation ann = method.getAnnotation(RequestCallback.class);

                //Get the name parameter of the annotation
                RequestCallback callback = (RequestCallback) ann;
                requestCallbacks.put(callback.name(), method);
            }
        }

        System.out.println(requestCallbacks);
    }

    /**
     * Handles a request, given the client and content passed from the client.
     * @param user the User
     * @param content the content sent by the client
     * @return true if the request was valid and handled, else false.
     */
    public String handleRequest(Server server, User user, String content) {

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

                    Object response = requestCallbacks.get(type).invoke(null, server, mongo, user, message);
                    return (String) response;

                } catch (InvocationTargetException | IllegalAccessException | IllegalFormatConversionException ex) {
                    ex.printStackTrace();
                    return ResponseUtil.error("Bad Request");
                }
            }
            //Return Bad Request if the request type doesn't exist
        }
        //Bad JSON Request
        catch(JSONParseException ex) {
            ex.printStackTrace();
        }
        return ResponseUtil.error("Bad Request");
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
