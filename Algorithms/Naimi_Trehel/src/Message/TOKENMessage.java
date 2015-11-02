package Message;

public class TOKENMessage extends NetMessage {

    private MsgType type;

    public TOKENMessage() {
        type = MsgType.TOKEN;
    }

    @Override
    public MsgType getMsgType() {
        return type;
    }

    @Override
    public visidia.simulation.process.messages.Message clone() {
        return new TOKENMessage();
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
