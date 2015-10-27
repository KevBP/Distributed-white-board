import Message.NetMessage;
import Message.REQMessage;
// Visidia imports
import visidia.simulation.process.messages.Door;
import visidia.simulation.process.messages.Message;

// Reception thread
public class ReceptionRules extends Thread {

    private RicartAgrawala algo;

    public ReceptionRules(RicartAgrawala a) {

        algo = a;
    }

    public void run() {

        Door d = new Door();

        while (true) {

            Message m = algo.recoit(d);
            int door = d.getNum();

            switch (((NetMessage) m).getMsgType()) {

                case REQ:
                    algo.receiveREQ(door, ((REQMessage) m).getH());
                    break;

                case REL:
                    algo.receiveREL(door);
                    break;

                default:
                    System.out.println("Error message type");
            }
        }
    }
}

