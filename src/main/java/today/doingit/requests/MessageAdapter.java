package today.doingit.requests;

import com.google.gson.JsonSyntaxException;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;

public class MessageAdapter extends TypeAdapter {

    @Override
    public Object read(final JsonReader jr) throws IOException {
        final Message message = new Message();
        jr.beginObject();

        while(jr.hasNext()) {
            //System.out.println(jr.nextName());
            switch(jr.nextName()) {
                case "username":
                    message.setUsername(jr.nextString());
                    break;
                case "group":
                    message.setGroup(jr.nextString());
                    break;
                case "message":
                    message.setMessage(jr.nextString());
                    break;
                default:
                    throw new InvalidMessageException("Cannot parse JSON to type Message because it is not of the correct format.");
            }
        }
        return message;
    }

    @Override
    public void write(final JsonWriter jw, Object o) throws IOException {
        if(o.getClass() != Message.class) {
            throw new JsonSyntaxException("Cannot convert Object to Message");
        }

        Message m = (Message)o;
        jw.beginObject();
        jw.name("username").value(m.getUsername());
        jw.name("group").value(m.getGroup());
        jw.name("message").value(m.getMessage());
        jw.endObject();
    }
}
