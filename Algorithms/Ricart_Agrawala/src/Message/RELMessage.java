package Message;

import visidia.simulation.process.messages.Message;

public class RELMessage extends NetMessage {

    private MsgType type;

    public RELMessage() {
        type = MsgType.REL;
    }

    @Override
    public MsgType getMsgType() {
        return type;
    }

    @Override
    public Message clone() {
        return new RELMessage();
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
