import Message.*;

// Visidia imports
import visidia.simulation.process.messages.Door;
import visidia.simulation.process.messages.Message;

// Reception thread
public class ReceptionRules extends Thread {

    private NaimiTrehel algo;

    public ReceptionRules(NaimiTrehel a) {

        algo = a;
    }

    public void run() {

        Door d = new Door();

        while (true) {

            Message m = algo.recoit(d);
            int door = d.getNum();

            switch (((NetMessage) m).getMsgType()) {

                case REQ:
                    algo.receiveREQ(door, ((REQMessage) m).getFrom());
                    break;

                case TOKEN:
                    algo.receiveTOKEN(door);
                    break;

                case INIT:
                    algo.initDoor(door, ((REQMessage) m).getFrom());
                    break;

                default:
                    System.out.println("Error message type");
            }
        }
    }
}

