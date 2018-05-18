package today.doingit.Server;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.channels.SocketChannel;
import java.util.HashMap;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import today.doingit.App.Request.RequestCallback;
import java.lang.annotation.Annotation;
import java.util.Set;



/*
    This is solely to avoid cluttering the Server class file.
    Mainly will be used for request handling inside the read method.

    This file needs a bit more cleaning
 */



public class RequestHandler {

    /*
        CLIENT INFORMATION
     */
    //This is a HashMap which allows messages waiting to be sent to be queued.

    private final static HashMap<String, Method> requestCallbacks = new HashMap<String, Method>();
    private static HashMap<String, Object> objects = new HashMap<String, Object>();

    public RequestHandler() {

        Reflections reflections = new Reflections(
            new ConfigurationBuilder()
                .setUrls(ClasspathHelper.forPackage("today.doingit.App"))
                .setScanners(new MethodAnnotationsScanner())
        );
        Set<Method> annotated = reflections.getMethodsAnnotatedWith(RequestCallback.class);

        for(Method method : annotated) {
            if (method.isAnnotationPresent(RequestCallback.class)) {

                Annotation ann = method.getAnnotation(RequestCallback.class);
                RequestCallback callback = (RequestCallback) ann;
                requestCallbacks.put(callback.name(), method);
            }
        }

        System.out.println(requestCallbacks);
    }

    public boolean handleRequest(SocketChannel client, String content) {
        JsonParser parser = new JsonParser();
        JsonObject obj = parser.parse(content.trim()).getAsJsonObject();

        JsonElement type = obj.get("type");
        JsonElement message = obj.get("message");
        System.out.println(type.getAsString());

        if(isValidRequest(type.getAsString())) {
            try {
                requestCallbacks.get(type.getAsString()).invoke(null, client, message.toString());
            }
            catch(InvocationTargetException ite) {
                ite.printStackTrace();
                System.exit(0);
            }
            catch(IllegalAccessException iae) {
                iae.printStackTrace();
                System.exit(0);
            }
            return true;
        }
        return false;
    }

    public boolean isValidRequest(String requestName) {
        return (requestCallbacks.containsKey(requestName));
    }


}
