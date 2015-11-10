package ricartagrawala;

import ricartagrawala.message.RELMessage;
import ricartagrawala.message.REQMessage;
import routing.RoutingAlgo;
import routing.message.SendToMessage;

import java.util.LinkedList;
import java.util.List;

public class Ricart_Agrawala extends RoutingAlgo {
    private int h;
    private int hSC;
    private boolean waitForCritical;
    private List<Integer> waiting;
    private int nbProcs;

    @Override
    public void setup() {
        h = 0;
        hSC = 0;
        waitForCritical = false;
        waiting = new LinkedList<Integer>();
        nbProcs = getNetSize();
    }

    @Override
    public void onMessage(SendToMessage message) {
        if (message.getData() instanceof REQMessage) { // Rule 2
            int hd = ((REQMessage) message.getData()).getH();
            h = Math.max(hd, h);
            if (waitForCritical && ((hSC < hd) || ((hSC == hd) && getId() < message.getFrom()))) {
                waiting.add(message.getFrom());
            }
            else {
                sendToNode(message.getFrom(), new RELMessage());
            }
        }
        else if (message.getData() instanceof  RELMessage) { // Rule 3
            nbProcs--;
        }
        else {
            throw new RuntimeException("Wrong type message");
        }
    }

    private synchronized void criticalStuff(){
        System.out.println("CRITICAL SUB");
    }

    @Override
    public Object clone() {
        return new Ricart_Agrawala();
    }
}
