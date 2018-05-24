package today.doingit.App.Response;

public class ResponseUtil {

    public static String error(String text) {
        return "{" +
                "\"type\":\"error\"," +
                "\"body\":\"" + text + "\"" +
                "}";
    }

    public static String generateResponse(String type, String text) {
        return "{" +
                "\"type\":\"" + type + "\"," +
                "\"body\":" + text +
                "}";
    }
}
