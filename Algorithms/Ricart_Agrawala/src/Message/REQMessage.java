package Message;

import visidia.simulation.process.messages.Message;

public class REQMessage extends NetMessage {

    private MsgType type;
    private int h;

    public REQMessage(int h) {
        type = MsgType.REQ;
        this.h = h;
    }

    @Override
    public MsgType getMsgType() {
        return type;
    }

    @Override
    public Message clone() {
        return new REQMessage(h);
    }

    @Override
    public String toString() {
        return type.toString();
    }

    @Override
    public String getData() {
        return this.toString();
    }

    public int getH() {
        return h;
    }
}
