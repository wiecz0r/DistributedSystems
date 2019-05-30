package messaging;

import java.io.Serializable;

public class Request implements Serializable {
    public String title;
    public MessageType type;

    public Request(String title, MessageType type) {
        this.title = title;
        this.type = type;
    }
}

