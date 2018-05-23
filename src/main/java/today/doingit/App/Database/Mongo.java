package today.doingit.App.Database;


import com.mongodb.*;

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
}
