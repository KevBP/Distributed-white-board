package routing.message;

/**
 * Created by maxime on 23/10/2015.
 */
public class SendToMessage extends RoutingMessage {
    private final int from;
    private final int to;
    private final Object data;

    public SendToMessage(int from, int to, Object data) {
        this.from = from;
        this.to = to;
        this.data = data;
    }

    public int getFrom() {
        return from;
    }

    public int getTo() {
        return to;
    }

    @Override
    public Object getData() {
        return data;
    }

    @Override
    public String toString() {
        return "SendToMessage{" +
                "from=" + from +
                ", to=" + to +
                ", data=" + data +
                '}';
    }

    @Override
    public Object clone() {
        return new SendToMessage(from, to, data);
    }
}
