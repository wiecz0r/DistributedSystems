package messaging;

import java.io.Serializable;
import java.util.List;

public class StreamResponse implements Serializable {
    private final String line;

    public StreamResponse(String line) {
        this.line = line;
    }

    public String getLine() {
        return line;
    }
}
