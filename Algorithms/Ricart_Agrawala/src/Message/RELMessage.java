package Message;

import visidia.simulation.process.messages.Message;

public class RELMessage extends NetMessage {

    private MsgType type;

    public RELMessage(MsgType t) {
        type = t;
    }

    @Override
    public MsgType getMsgType() {
        return type;
    }

    @Override
    public Message clone() {
        return new RELMessage(type);
    }

    @Override
    public String toString() {
        return type.toString();
    }

    @Override
    public String getData() {
        return this.toString();
    }

}
