package lelann;

import ring.Token;
import routing.RoutingAlgo;
import routing.message.SendToMessage;

public class LeLann extends RoutingAlgo {


    @Override
    public void setup() {
        if (getId() == 0){
            sendToNode(0, new Token());
        }
    }

    @Override
    public void onMessage(SendToMessage message) {
        if (message.getData() instanceof Token){
            criticalStuff();
        }
        sendToNode(nextNode(), message.getData());
    }


    private synchronized void criticalStuff(){
        System.out.println("CRITICAL SUB");
    }

    public int nextNode(){
        return (getId() + 1) % getNetSize();
    }

    @Override
    public Object clone() {
        return new LeLann();
    }
}
