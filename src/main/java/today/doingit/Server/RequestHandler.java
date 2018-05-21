package today.doingit.Server;

//Networking
import java.nio.channels.SelectionKey;

//Data structures
import java.util.HashMap;
import java.util.IllegalFormatConversionException;
import java.util.Set;

//Google's JSON
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

//Reflections/Annotation
import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

//Internal
import today.doingit.App.Request.RequestCallback;




public class RequestHandler {


    private final static HashMap<String, Method> requestCallbacks = new HashMap<String, Method>();

    /**
     * Constructor which when initialized will examine the classpath and generate a HashMap
     * containing all methods inside classes within the package, today.doingit.App that have the
     * {@link RequestCallback} annotation.
     */
    public RequestHandler() {

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
     * @param client the client's channel
     * @param content the content sent by the client
     * @return true if the request was valid and handled, else false.
     */
    public String handleRequest(Server server, SelectionKey client, String content) {

        //Get the request TYPE & request MESSAGE
        JsonParser parser = new JsonParser();
        JsonObject obj = parser.parse(content.trim()).getAsJsonObject();

        JsonElement type = obj.get("type");
        JsonElement message = obj.get("message");


        //It is considered a valid request if the request type exists in RequestCallbacks
        if(isValidRequest(type.getAsString())) {

            //Invoke the request type's callback. i.e if type = authorization, invoke OnMessageRequest in Authorization
            try {
                Object response = requestCallbacks.get(type.getAsString()).invoke(null, server, client, message.toString());
                return (String) response;
            }
            catch(InvocationTargetException ite) {
                ite.printStackTrace();
                System.exit(0);
            }
            catch(IllegalAccessException iae) {
                iae.printStackTrace();
                System.exit(0);
            }
            catch(IllegalFormatConversionException e) {
                e.printStackTrace();
                System.exit(0);
            }
        }
        //TODO: change to proper error later
        return "{" +
                "\"type\":\"broadcast\",\"body\":\"INVALID!\"" +
                "}";
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
