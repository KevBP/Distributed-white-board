package Message;

import visidia.simulation.process.messages.Message;

public abstract class NetMessage extends Message {

    private MsgType type;

    public MsgType getMsgType() {
        return type;
    }
}
