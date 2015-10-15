
import visidia.simulation.process.messages.Message;
import visidia.simulation.process.messages.MessageType;

public class SyncMessage extends Message {

    MsgType type;
    int proc;

    public SyncMessage() {
        MessageType mt = new MessageType("Msg");
        mt.setColor(java.awt.Color.black);
        this.setType(mt);
    }

    public SyncMessage(MsgType t, int p) {

        System.out.println(t);
        type = t;
        proc = p;
        MessageType mt = new MessageType("Msg");

        switch (type) {
            case TOKEN:
                mt.setColor(java.awt.Color.cyan);
                this.setType(mt);
                break;
        }
    }

    public MsgType getMsgType() {

        return type;
    }

    @Override
    public Message clone() {
        return new SyncMessage();
    }

    @Override
    public String getData() {

        return type.toString();
    }

    public int getMsgProc() {
        return proc;
    }

    @Override
    public String toString() {

        return type.toString() + "_" + proc;
    }
}
