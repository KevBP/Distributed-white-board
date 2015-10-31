package Message;

public class REQMessage extends NetMessage {

    private MsgType type;
    private int from;

    public REQMessage(int f) {
        this(MsgType.REQ, f);
    }

    public REQMessage(MsgType t, int f) {
        type = t;
        from = f;
    }

    @Override
    public MsgType getMsgType() {
        return type;
    }

    @Override
    public visidia.simulation.process.messages.Message clone() {
        return new REQMessage(from);
    }

    @Override
    public String toString() {
        return type.toString();
    }

    @Override
    public String getData() {
        return this.toString();
    }

    public int getFrom() {
        return from;
    }
}
