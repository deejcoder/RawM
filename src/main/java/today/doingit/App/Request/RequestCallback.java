package today.doingit.App.Request;

import java.lang.annotation.*;

/**
 * Annotates a method as a data 'receiver'. It will
 * be passed data from the RequestHandler when the message type is
 * equal.
 * Name is the identifer of the request type.
 */
@Target({ElementType.TYPE})
@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface RequestCallback {
    String name();
}
