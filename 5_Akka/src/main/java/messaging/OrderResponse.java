package messages;

public class OrderResponse extends Response{
    private boolean result;

    public OrderResponse(boolean result) {
        this.result = result;
    }
}
