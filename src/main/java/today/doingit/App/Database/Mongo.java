package today.doingit.App.Database;


import com.mongodb.*;
import java.time.LocalDateTime;

public class Mongo {

    //The connection handle
    private MongoClient mongo;

    //The database
    private DB db;

    //The users collection
    private DBCollection users;

    /**
     * Connects to a given Mongo server's database.
     * @param URI the URI of the Mongo server
     * @param database the name of the database
     */
    public Mongo(String URI, String database) {

        //Connect to the DB server with the provided URI
        mongo = new MongoClient(new MongoClientURI(URI));

        //Get the provided database
        db = mongo.getDB(database);

        //Get the users collection
        users = db.getCollection("users");
        System.out.println(users.findOne());
    }

    /**
     * Checks the Users collection in Mongo, for a provided
     * username.
     * @param username the username, given as a String.
     * @return true if the username was found, otherwise false.
     */
    public boolean validUser(String username) {

        //Search the collection for the provided username
        DBObject query = new BasicDBObject("username", username);
        DBCursor cursor = users.find(query);

        //Was one found?
        if(cursor.one() != null) {
            cursor.one();
            return true;
        }
        return false;
    }

    /**
     * Creates a new user and inserts it to the Mongo database.
     * @param username the username of the new user
     * @param password the password of the new user
     * @param description the user's profile description
     * @return true or false depending on if the user was successfully created.
     */
    public boolean createUser(String username, String password, String description) {

        try {
            BasicDBObjectBuilder document = BasicDBObjectBuilder.start()
                    .add("username", username)
                    .add("password", password)
                    .add("description", description)
                    .add("createdDate", LocalDateTime.now());

            users.insert(document.get());
        }
        catch(MongoException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
}
