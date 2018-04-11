package today.doingit.requests;



/*
    Throws when a JSON object is not of type Message.
 */
public class InvalidMessageException extends RuntimeException {
    public InvalidMessageException() { super(); }
    public InvalidMessageException(String message) { super(message); }
    public InvalidMessageException(String message, Throwable cause) { super(message, cause); }
    public InvalidMessageException(Throwable cause) { super(cause); }
}
