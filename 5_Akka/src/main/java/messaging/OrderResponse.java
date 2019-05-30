package messaging;

import java.io.Serializable;

public class OrderResponse implements Serializable {
    public boolean result;

    public OrderResponse(boolean result) {
        this.result = result;
    }
}
