package messaging;

public class SearchDatabaseRequest extends Request {
    public String database;

    public SearchDatabaseRequest(String title, MessageType type, String database) {
        super(title, type);
        this.database = database;
    }
}
