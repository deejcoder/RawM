package today.doingit.App.Response;

public class Error {

    public static String error(String text) {
        return "{" +
                "\"type\":\"error\"," +
                "\"body\":\"" + text + "\"" +
                "}";
    }
}
