package routing.message;

import visidia.simulation.process.messages.MessageType;

import java.awt.*;

public class SendToMessage extends RoutingMessage {
    public final static MessageType SEND_TO_MESSAGE_TYPE = new MessageType("routing", true, Color.RED);
    private final int from;
    private final int to;
    private final Object data;

    public SendToMessage(int from, int to, Object data) {
        setType(SEND_TO_MESSAGE_TYPE);
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
